package com.hadou;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.JSONOutputter;
import edu.stanford.nlp.pipeline.ProtobufAnnotationSerializer;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by jiajianchao on 2017/3/16.
 */
public class CustomerCoreNLPClient extends StanfordCoreNLPClient {
    private static final Redwood.RedwoodChannels log = Redwood.channels(CustomerCoreNLPClient.class);
    private final BackendScheduler scheduler;
    private final Properties properties;
    private final String propsAsJSON;
    private final ProtobufAnnotationSerializer serializer = new ProtobufAnnotationSerializer(true);

    public CustomerCoreNLPClient(Properties properties, String host, int port, int threads) {
        super(properties, host, port, threads);
        List<Backend> backends = new ArrayList<Backend>();
        for (int i = 0; i < threads; ++i) {
            backends.add(new Backend(host.startsWith("http://") ? "http" : "https",
                    host.startsWith("http://") ? host.substring("http://".length()) : (host.startsWith("https://") ? host.substring("https://".length()) : host),
                    port));
        }
        // Start 'er up
        this.properties = properties;
        Properties serverProperties = new Properties();
        for (String key : properties.stringPropertyNames()) {
            serverProperties.setProperty(key, properties.getProperty(key));
        }
        Collections.shuffle(backends, new Random(System.currentTimeMillis()));
        this.scheduler = new BackendScheduler(backends);

        // Set required serverProperties
        serverProperties.setProperty("inputFormat", "serialized");
        serverProperties.setProperty("outputFormat", "serialized");
        serverProperties.setProperty("inputSerializer", ProtobufAnnotationSerializer.class.getName());
        serverProperties.setProperty("outputSerializer", ProtobufAnnotationSerializer.class.getName());

        // Create a list of all the properties, as JSON map elements
        List<String> jsonProperties = serverProperties.stringPropertyNames().stream().map(key -> '"' + JSONOutputter.cleanJSON(key) + "\": \"" +
                JSONOutputter
                        .cleanJSON(serverProperties.getProperty(key)) + '"')
                .collect(Collectors.toList());
        // Create the JSON object
        this.propsAsJSON = "{ " + StringUtils.join(jsonProperties, ", ") + " }";
        this.scheduler.start();
    }

    @Override
    public void annotate(final Annotation annotation, final Consumer<Annotation> callback) {
        scheduler.schedule((Backend backend, Consumer<Backend> isFinishedCallback) -> new Thread(() -> {
            try {
                // 1. Create the input
                // 1.1 Create a protocol buffer
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                serializer.write(annotation, os);
                os.close();
                byte[] message = os.toByteArray();
                // 1.2 Create the query params

                String queryParams = String.format(
                        "properties=%s",
                        URLEncoder.encode(this.propsAsJSON, "utf-8"));

                // 2. Create a connection
                URL serverURL = new URL(backend.protocol, backend.host,
                        backend.port,
                        '?' + queryParams);

                // 3. Do the annotation
                //    This method has two contracts:
                //    1. It should call the two relevant callbacks
                //    2. It must not throw an exception
                doAnnotation(annotation, backend, serverURL, message, 0);
            } catch (Throwable t) {
                log.warn("Could not annotate via server! ");
            } finally {
                callback.accept(annotation);
                isFinishedCallback.accept(backend);
            }
        }).start());
    }

    private void doAnnotation(Annotation annotation, Backend backend, URL serverURL, byte[] message, int tries) {

        try {
            // 1. Set up the connection
            URLConnection connection = serverURL.openConnection();
            // 1.1 Set authentication
            // 1.2 Set some protocol-independent properties
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-protobuf");
            connection.setRequestProperty("Content-Length", Integer.toString(message.length));
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setRequestProperty("User-Agent", StanfordCoreNLPClient.class.getName());
            // 1.3 Set some protocol-dependent properties
            switch (backend.protocol) {
                case "https":
                case "http":
                    ((HttpURLConnection) connection).setRequestMethod("POST");
                    break;
                default:
                    throw new IllegalStateException("Haven't implemented protocol: " + backend.protocol);
            }

            // 2. Annotate
            // 2.1. Fire off the request
            connection.connect();
            connection.getOutputStream().write(message);
            connection.getOutputStream().flush();
            // 2.2 Await a response
            // -- It might be possible to send more than one message, but we are not going to do that.
            Annotation response = serializer.read(connection.getInputStream()).first;
            // 2.3. Copy response over to original annotation
            for (Class key : response.keySet()) {
                annotation.set(key, response.get(key));
            }

        } catch (Throwable t) {
            // 3. We encountered an error -- retry
            log.warn(t.getMessage());
            throw new RuntimeException(t);
        }
    }

    private static class BackendScheduler extends Thread {
        /**
         * The list of backends that we can schedule on.
         * This should not generally be called directly from anywhere
         */
        public final List<Backend> backends;

        /**
         * The queue on requests for the scheduler to handle.
         * Each element of this queue is a function: calling the function signals
         * that this backend is available to perform a task on the passed backend.
         * It is then obligated to call the passed Consumer to signal that it has
         * released control of the backend, and it can be used for other things.
         * Remember to lock access to this object with {@link StanfordCoreNLPClient.BackendScheduler#stateLock}.
         */
        private final Queue<BiConsumer<Backend, Consumer<Backend>>> queue;
        /**
         * The lock on access to {@link StanfordCoreNLPClient.BackendScheduler#queue}.
         */
        private final Lock stateLock = new ReentrantLock();
        /**
         * Represents the event that an item has been added to the work queue.
         * Linked to {@link StanfordCoreNLPClient.BackendScheduler#stateLock}.
         */
        private final Condition enqueued = stateLock.newCondition();
        /**
         * Represents the event that the queue has become empty, and this schedule is no
         * longer needed.
         */
        public final Condition shouldShutdown = stateLock.newCondition();

        /**
         * The queue of annotators (backends) that are free to be run on.
         * Remember to lock access to this object with {@link StanfordCoreNLPClient.BackendScheduler#stateLock}.
         */
        private final Queue<Backend> freeAnnotators;
        /**
         * Represents the event that an annotator has freed up and is available for
         * work on the {@link StanfordCoreNLPClient.BackendScheduler#freeAnnotators} queue.
         * Linked to {@link StanfordCoreNLPClient.BackendScheduler#stateLock}.
         */
        private final Condition newlyFree = stateLock.newCondition();

        /**
         * While this is true, continue running the scheduler.
         */
        private boolean doRun = true;

        /**
         * Create a new scheduler from a list of backends.
         * These can contain duplicates -- in that case, that many concurrent
         * calls can be made to that backend.
         */
        public BackendScheduler(List<Backend> backends) {
            super();
            setDaemon(true);
            this.backends = backends;
            this.freeAnnotators = new LinkedList<>(backends);
            this.queue = new LinkedList<>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                while (doRun) {
                    // Wait for a request
                    BiConsumer<Backend, Consumer<Backend>> request;
                    Backend annotator;
                    stateLock.lock();
                    try {
                        while (queue.isEmpty()) {
                            enqueued.await();
                            if (!doRun) {
                                return;
                            }
                        }
                        // Get the actual request
                        request = queue.poll();
                        // We have a request

                        // Find a free annotator
                        while (freeAnnotators.isEmpty()) {
                            newlyFree.await();
                        }
                        annotator = freeAnnotators.poll();
                    } finally {
                        stateLock.unlock();
                    }
                    // We have an annotator

                    // Run the annotation
                    request.accept(annotator, freedAnnotator -> {
                        // ASYNC: we've freed this annotator
                        // add it back to the queue and register it as available
                        stateLock.lock();
                        try {
                            freeAnnotators.add(freedAnnotator);

                            // If the queue is empty, and all the annotators have returned, we're done
                            if (queue.isEmpty() && freeAnnotators.size() == backends.size()) {
                                log.debug("All annotations completed. Signaling for shutdown");
                                shouldShutdown.signalAll();
                            }

                            newlyFree.signal();
                        } finally {
                            stateLock.unlock();
                        }
                    });
                    // Annotator is running (in parallel, most likely)
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Schedule a new job on the backend
         *
         * @param annotate A callback, which will be called when a backend is free
         *                 to do some processing. The implementation of this callback
         *                 MUST CALL the second argument when it is done processing,
         *                 to register the backend as free for further work.
         */
        public void schedule(BiConsumer<Backend, Consumer<Backend>> annotate) {
            stateLock.lock();
            try {
                queue.add(annotate);
                enqueued.signal();
            } finally {
                stateLock.unlock();
            }
        }
    } // end static class BackEndScheduler

    private static class Backend {
        /**
         * The protocol to connect to the server with.
         */
        public final String protocol;
        /**
         * The hostname of the server running the CoreNLP annotators
         */
        public final String host;
        /**
         * The port of the server running the CoreNLP annotators
         */
        public final int port;

        public Backend(String protocol, String host, int port) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Backend)) return false;
            Backend backend = (Backend) o;
            return port == backend.port && protocol.equals(backend.protocol) && host.equals(backend.host);
        }

        @Override
        public int hashCode() {
            throw new IllegalStateException("Hashing backends is dangerous!");
        }

        @Override
        public String toString() {
            return protocol + "://" + host + ":" + port;
        }
    }
}

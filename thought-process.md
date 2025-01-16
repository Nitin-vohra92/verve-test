# Thought Process For Application

![hld_verve.png](/hld_verve.png)
## 1. Handling Deduplication

### Initial Approach
- **Local Caching**: For a basic application, a `ConcurrentHashMap` could be used for caching unique request IDs.
- **Limitation**: This approach is not scalable for distributed systems.

### Scalable Solution
- **Distributed Caching**: Redis was chosen to manage deduplication.
    - **Key Design**: Keys are created based on the time (e.g., minute-level granularity).
    - **Value Storage**: Request IDs are added to Redis sets. Since sets inherently maintain uniqueness, this ensures data deduplication.
    - **Advantages**:
        - Scalable and distributed.
        - Efficient handling of unique entries in a high-concurrency environment.

## 2. Extending for GET and POST Requests

### Rationale
- Both GET and POST methods were implemented to provide flexibility.
    - **GET Requests**: To handle simple endpoint interactions.
    - **POST Requests**: To send payloads (e.g., unique request counts) to external services.

### Benefits
- This design allows the application to adapt easily to different requirements without significant code changes.

## 3. Writing Count of Unique IDs
- Configured to use either Kafka or a log file as a sink for processing data.
  - The process is currently scheduled to run every minute, but multiple syncs may occur if different instances are involved. To handle such cases efficiently, Redis locks can be utilized to ensure that each instance checks whether the task has already been processed or not, preventing duplicate operations.
      ### Why Kafka?
    - Kafka was chosen to handle the stats output due to its robustness and scalability.

    ### Key Considerations
      - **Durability**: Kafka ensures that messages are not lost.
      - **Speed**: It can handle high-throughput scenarios efficiently.
      - **Error Handling**:
          - Dead-letter queues can be configured to manage failures gracefully.
          - Ensures reliable delivery of stats even in the face of transient errors.

## Conclusion
- The application design prioritizes scalability, reliability, and extensibility.
- Redis and Kafka together provide a robust foundation for handling deduplication and stats logging.
- Support for both GET and POST requests ensures that the system can evolve to meet future requirements.
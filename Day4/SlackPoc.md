
# A Highly Scalable Chat Application Using Redis Pub/Sub and WebSockets

The application uses a combination of Redis Pub/Sub and WebSocket to allow a horizontally scalable architecture. The application hosts a WebSocket server endpoint that clients (users) can connect to using a WebSocket client.

Here’s the high-level application flow:

1. A user connects to an endpoint provided by the application. If successful, this creates a WebSocket connection between the client and application (server).
2. When the user sends a chat message, it’s relayed over the WebSocket connection to the application.
3. The application publishes this message to the Redis channel.
4. Because the application is also subscribed to this channel, it receives this message and sends the messages to the users of that particular channel (via the WebSocket connection that was established initially).

Note that the server also acts as a Pub/Sub client. We create a Redis Pub/Sub channel for broadcasting chat messages. The application sends data to this Redis channel and also receives data from the channel via a Pub/Sub subscription.

## Prerequisites

### Set Up Redis

1. Install Docker Desktop for Windows from this page - [Docker Desktop for Windows](https://docs.docker.com/desktop/windows/install/)
2. At your terminal, run:

   ```bash
   docker run -d --name redis-stack-server -p 6379:6379 redis/redis-stack-server:latest
   ```

3. To connect to your local Redis server and execute commands, run:

   ```bash
   docker exec -it redis-stack-server redis-cli
   ```

### Steps to Run the Chat Application

1. Open the `slackpoc-ui` in VS Code and start two instances which will connect to two different instances of our backend servers.

   Commands to run:

   ```bash
   npm run start:env1
   npm run start:env2
   ```

2. Start two instances of `slackpoc` backend by specifying different server and WebSocket ports through VM args.

   VM args for the second app:

   ```bash
   -Dserver.port=8082 -Dwebsocket.port=8082
   ```

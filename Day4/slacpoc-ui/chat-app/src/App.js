import React, { useState, useEffect, useRef } from "react";

const App = () => {
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [channel, setChannel] = useState("");
  const [userId] = useState(() => "user" + Math.floor(Math.random() * 1000));
  const socketRef = useRef(null);
  const alertShownRef = useRef(false);

  useEffect(() => {
    const wsPort = process.env.REACT_APP_WS_PORT || "8090";
    let enteredChannel = null;
    if (!channel && !alertShownRef.current) {
      enteredChannel = prompt("Please enter a channel number:");
      if (!enteredChannel) return; // If user cancels, do nothing
      setChannel(enteredChannel);
      alertShownRef.current = true;
    }

    if (!socketRef.current) {
      socketRef.current = new WebSocket(
        `ws://localhost:${wsPort}/ws?userId=${userId}&channel=${enteredChannel}`
      );

      socketRef.current.onopen = () => {
        console.log("Connected to WebSocket");
      };

      socketRef.current.onmessage = (event) => {
        const messageData = JSON.parse(event.data);
        setMessages((prevMessages) => [...prevMessages, messageData]);
      };

      socketRef.current.onerror = (error) => {
        console.error("WebSocket error:", error);
      };

      socketRef.current.onclose = (event) => {
        console.log("WebSocket closed:", event);
      };
    }

    const handleBeforeUnload = () => {
      if (socketRef.current) {
        socketRef.current.close();
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [userId, channel]);

  const sendMessage = () => {
    if (socketRef.current && message.trim() !== "") {
      const messageObject = {
        userId: userId,
        message: message,
        channel: channel,
      };
      socketRef.current.send(JSON.stringify(messageObject));
      setMessage("");
    }
  };

  return (
    <div>
      <h1>Chat</h1>
      <div>
        <div>User ID: {userId}</div>
        <div>Channel: {channel}</div>
        <input
          id="message"
          type="text"
          placeholder="Enter message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />
        <button onClick={sendMessage}>Send</button>
      </div>
      <div id="messages">
        {messages.map((msg, index) => (
          <p key={index}>
            {msg.userId}: {msg.message}
          </p>
        ))}
      </div>
    </div>
  );
};

export default App;

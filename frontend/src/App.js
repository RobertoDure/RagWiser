import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { FiPaperclip, FiSend } from 'react-icons/fi';
import ReactMarkdown from 'react-markdown';
import './App.css';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState('');
  const messageEndRef = useRef(null);
  const fileInputRef = useRef(null);

  // Auto scroll to bottom when messages change
  useEffect(() => {
    if (messageEndRef.current) {
      messageEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  // Handle file upload
  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Check if file is a PDF
    if (file.type !== 'application/pdf') {
      setUploadStatus('Error: Please select a PDF file');
      return;
    }

    setUploadStatus(`Uploading ${file.name}...`);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await axios.post('http://localhost:8080/api/rag/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      console.log('Upload response:', response);
      setUploadStatus(`Success: ${response.data}`);
      // Add a system message indicating successful upload
      setMessages(prevMessages => [
        ...prevMessages,
        {
          role: 'system',
          content: `PDF "${file.name}" has been uploaded and processed successfully. You can now ask questions about it!`
        }
      ]);
    } catch (error) {
      console.error('Error uploading file:', error);
      setUploadStatus(`Error: ${error.response?.data || error.message || 'Failed to upload file'}`);
    }

    // Clear the file input
    event.target.value = null;
  };

  // Handle sending a message
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = input.trim();
    setInput('');

    // Add user message to chat
    setMessages(prevMessages => [...prevMessages, { role: 'user', content: userMessage }]);

    // Show loading state
    setLoading(true);

    try {
      console.log('Sending request to API with question:', userMessage);

      // Use encodeURIComponent to properly encode the question parameter
      const encodedQuestion = encodeURIComponent(userMessage);
      const url = `http://localhost:8080/api/rag?question=${encodedQuestion}`;

      console.log('Request URL:', url);

      const response = await axios.get(url);
      console.log('API response:', response);

      if (response.data) {
        // Add AI response to chat
        setMessages(prevMessages => [
          ...prevMessages,
          { role: 'assistant', content: response.data }
        ]);
      } else {
        throw new Error('Empty response received from server');
      }
    } catch (error) {
      console.error('Error getting response:', error);

      // Log detailed error information
      if (error.response) {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        console.error('Error response data:', error.response.data);
        console.error('Error response status:', error.response.status);
        console.error('Error response headers:', error.response.headers);
      } else if (error.request) {
        // The request was made but no response was received
        console.error('Error request:', error.request);
      }

      setMessages(prevMessages => [
        ...prevMessages,
        {
          role: 'assistant',
          content: `Sorry, I encountered an error while processing your question: ${error.message || 'Unknown error'}. Please try again later.`
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  // Trigger file input click
  const triggerFileUpload = () => {
    fileInputRef.current.click();
  };

  return (
    <div className="app">
      <header className="header">
        <h1>RagWiser</h1>
        <p>Upload PDF documents and chat with your data</p>
      </header>

      <div className="file-upload-section">
        <div className="file-upload-container">
          <button
            className="upload-button"
            onClick={triggerFileUpload}
          >
            <FiPaperclip className="upload-icon" />
            Upload PDF
          </button>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            accept=".pdf"
            style={{ display: 'none' }}
          />
          {uploadStatus && <div className="upload-status">{uploadStatus}</div>}
        </div>
      </div>

      <div className="chat-container">
        <div className="messages">
          {messages.length === 0 ? (
            <div className="welcome-message">
              <h2>Welcome to RagWiser</h2>
              <p>Upload a PDF document first, then ask questions about its content.</p>
              <p>The system will use RAG (Retrieval Augmented Generation) to provide accurate answers based on your documents.</p>
            </div>
          ) : (
            messages.map((message, index) => (
              <div
                key={index}
                className={`message ${message.role === 'user' ? 'user-message' : 'ai-message'}`}
              >
                <div className="message-avatar">
                  {message.role === 'user' ? 'You' : message.role === 'system' ? 'System' : 'AI'}
                </div>
                <div className="message-content">
                  <ReactMarkdown>
                    {message.content}
                  </ReactMarkdown>
                </div>
              </div>
            ))
          )}
          {loading && (
            <div className="message ai-message">
              <div className="message-avatar">AI</div>
              <div className="message-content loading">
                <div className="loading-dot"></div>
                <div className="loading-dot"></div>
                <div className="loading-dot"></div>
              </div>
            </div>
          )}
          <div ref={messageEndRef} />
        </div>

        <form className="input-area" onSubmit={handleSendMessage}>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask something about your documents..."
            disabled={loading}
          />
          <button type="submit" disabled={!input.trim() || loading}>
            <FiSend />
          </button>
        </form>
      </div>
    </div>
  );
}

export default App;

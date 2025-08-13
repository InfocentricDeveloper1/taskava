import { useState, useEffect } from 'react'
import './simple.css'
import { testHealthEndpoint, testSwaggerDocs } from './services/api'

function App() {
  const [healthStatus, setHealthStatus] = useState<any>(null)
  const [apiEndpoints, setApiEndpoints] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function checkBackendConnection() {
      setLoading(true)
      
      // Test health endpoint
      const health = await testHealthEndpoint()
      setHealthStatus(health)
      
      // Test API docs
      const docs = await testSwaggerDocs()
      setApiEndpoints(docs)
      
      setLoading(false)
    }
    
    checkBackendConnection()
  }, [])

  return (
    <div className="App">
      <h1>Taskava Frontend</h1>
      
      <div className="card">
        <h2>Backend Connection Test</h2>
        
        {loading ? (
          <p>Testing connection...</p>
        ) : (
          <>
            <div style={{ marginBottom: '20px', textAlign: 'left' }}>
              <h3>Health Check:</h3>
              <pre style={{ background: '#f0f0f0', padding: '10px', borderRadius: '5px' }}>
                {JSON.stringify(healthStatus, null, 2)}
              </pre>
            </div>
            
            <div style={{ textAlign: 'left' }}>
              <h3>Available API Endpoints:</h3>
              <pre style={{ background: '#f0f0f0', padding: '10px', borderRadius: '5px' }}>
                {JSON.stringify(apiEndpoints, null, 2)}
              </pre>
            </div>
          </>
        )}
      </div>
      
      <p className="read-the-docs">
        Backend API: http://localhost:8080/api
      </p>
    </div>
  )
}

export default App
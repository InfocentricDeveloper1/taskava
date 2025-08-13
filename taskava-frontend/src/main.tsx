import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AppWithShadcn from './AppWithShadcn'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppWithShadcn />
  </StrictMode>,
)

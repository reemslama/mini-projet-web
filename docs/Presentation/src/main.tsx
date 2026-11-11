import { createRoot } from 'react-dom/client'

// reveal.js core + base theme, then our custom Arène theme last so it wins.
import 'reveal.js/reveal.css'
import 'reveal.js/theme/black.css'
import './theme.css'

import App from './App'

// Note: intentionally NOT wrapped in <StrictMode>.
// StrictMode double-mounts components, which re-initializes reveal.js
// and breaks the deck (duplicated slides / lost state).
createRoot(document.getElementById('root')!).render(<App />)

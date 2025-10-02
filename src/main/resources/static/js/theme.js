// ===== GLOBAL THEME SYSTEM =====

function toggleTheme() {
    const body = document.body;
    const currentTheme = localStorage.getItem('theme') || 'dark';
    
    if (currentTheme === 'dark') {
        body.classList.add('light-mode');
        localStorage.setItem('theme', 'light');
        
        // Trigger custom event for other components
        window.dispatchEvent(new CustomEvent('themeChanged', { 
            detail: { theme: 'light' } 
        }));
    } else {
        body.classList.remove('light-mode');
        localStorage.setItem('theme', 'dark');
        
        // Trigger custom event for other components
        window.dispatchEvent(new CustomEvent('themeChanged', { 
            detail: { theme: 'dark' } 
        }));
    }
    
    // Redetectar páginas tributarias después del cambio de tema
    detectAndApplyTributarioTheme();
}

// Apply saved theme on page load
function initializeTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-mode');
    }
    
    // Detectar si estamos en una página tributaria y aplicar comportamiento inverso
    detectAndApplyTributarioTheme();
    
    // Trigger theme initialization event
    window.dispatchEvent(new CustomEvent('themeInitialized', { 
        detail: { theme: savedTheme } 
    }));
}

// Función para detectar páginas tributarias y aplicar tema inverso
function detectAndApplyTributarioTheme() {
    const currentPath = window.location.pathname;
    const isTributarioPage = currentPath.includes('/tributario') || 
                            currentPath.includes('/contribuyentes') ||
                            currentPath.includes('/impuestos') ||
                            currentPath.includes('/comprobantes') ||
                            currentPath.includes('/retenciones') ||
                            currentPath.includes('/declaraciones');
    
    if (isTributarioPage) {
        document.body.classList.add('tributario-panel');
        console.log('Panel tributario detectado - aplicando tema inverso');
    } else {
        document.body.classList.remove('tributario-panel');
    }
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', initializeTheme);

// Listen for storage changes (for cross-tab synchronization)
window.addEventListener('storage', function(e) {
    if (e.key === 'theme') {
        const newTheme = e.newValue || 'dark';
        if (newTheme === 'light') {
            document.body.classList.add('light-mode');
        } else {
            document.body.classList.remove('light-mode');
        }
        // Redetectar páginas tributarias después del cambio de tema
        detectAndApplyTributarioTheme();
    }
});

// Export functions for global use
window.toggleTheme = toggleTheme;
window.initializeTheme = initializeTheme;

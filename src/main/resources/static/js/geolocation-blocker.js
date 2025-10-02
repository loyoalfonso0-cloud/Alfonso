// Bloquear solicitudes de geolocalización en toda la aplicación
(function() {
    'use strict';
    
    // Sobrescribir las funciones de geolocalización para evitar solicitudes no deseadas
    if (navigator.geolocation) {
        const originalGetCurrentPosition = navigator.geolocation.getCurrentPosition;
        const originalWatchPosition = navigator.geolocation.watchPosition;
        
        navigator.geolocation.getCurrentPosition = function(success, error, options) {
            console.warn('Solicitud de geolocalización bloqueada');
            if (error) {
                error({
                    code: 1,
                    message: 'Geolocalización deshabilitada por la aplicación'
                });
            }
        };
        
        navigator.geolocation.watchPosition = function(success, error, options) {
            console.warn('Solicitud de seguimiento de ubicación bloqueada');
            if (error) {
                error({
                    code: 1,
                    message: 'Seguimiento de ubicación deshabilitado por la aplicación'
                });
            }
            return -1;
        };
    }
    
    // Bloquear cualquier intento de acceso a la ubicación
    Object.defineProperty(navigator, 'geolocation', {
        get: function() {
            return {
                getCurrentPosition: function(success, error) {
                    if (error) {
                        error({
                            code: 1,
                            message: 'Geolocalización no disponible'
                        });
                    }
                },
                watchPosition: function(success, error) {
                    if (error) {
                        error({
                            code: 1,
                            message: 'Seguimiento de ubicación no disponible'
                        });
                    }
                    return -1;
                },
                clearWatch: function() {}
            };
        },
        configurable: false
    });
    
})();

(function() {
    try {
        addEventListener('message', function () {
            if ( event.data.id !== 'patterns' ) {
            return;
        }

        const patterns = event.data.patterns || {};

        var js = {};

        for ( var appName in patterns ) {
            if ( patterns.hasOwnProperty(appName) ) {
                js[appName] = {};

                for ( var chain in patterns[appName] ) {
                    if ( patterns[appName].hasOwnProperty(chain) ) {
                        js[appName][chain] = {};

                        for ( var index in patterns[appName][chain] ) {
                            const value = detectJs(chain);

                            if ( value ) {
                                js[appName][chain][index] = value;
                            }
                        }
                    }
                }
            }
        }

        // postMessage({ id: 'js', js }, '*');
    })
    }catch(e){
        // Fail quietly
    }

    function detectJs(chain) {
        try {
            const properties = chain.split('.');

            var value = properties.length ? window : null;

            for ( var i = 0; i < properties.length; i ++ ) {
                var property = properties[i];

                if ( value.hasOwnProperty(property) ) {
                    value = value[property];
                } else {
                    value = null;

                    break;
                }
            }

            return typeof value === 'string' ? value : !!value;
        } catch(e) {
            // Fail quietly
        }
    }
}());
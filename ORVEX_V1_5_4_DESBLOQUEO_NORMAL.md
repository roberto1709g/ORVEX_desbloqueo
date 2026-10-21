# ORVEX V1.5.4 — Pantalla de bloqueo normal y desbloqueo manual

## Objetivo
Corregir el comportamiento observado en Xiaomi Redmi 12C cuando la pantalla está bloqueada: V1.5.3 encendía la pantalla, pero la actividad translúcida podía quedar por encima de la pantalla de bloqueo e impedir el gesto normal.

## Cambio principal
- La actividad de transición sigue solicitando que la pantalla se encienda.
- Se elimina `showWhenLocked` y no se usa `setShowWhenLocked(true)`.
- No se solicita descartar automáticamente el bloqueo.
- Si el teléfono sigue bloqueado, la actividad espera; después del desbloqueo normal, vuelve a comprobar el estado y abre WhatsApp.
- Se mantiene la comprobación de que el teléfono esté desbloqueado antes de abrir WhatsApp.
- `versionCode 10`, `versionName 1.5.4`.

## Se conserva
- Reconocimiento y confirmación por voz.
- Contactos y validación del número.
- Apertura de WhatsApp cuando ORVEX está en primer plano o en segundo plano con el teléfono desbloqueado.
- Servicio de accesibilidad que automatiza el menú de llamada.
- Identificador de paquete `com.leaden2.app`.

## Validación pendiente
Este ZIP es código fuente, no APK. Compilar mediante GitHub Actions y probar:
1. ORVEX en primer plano.
2. ORVEX en segundo plano con teléfono desbloqueado.
3. Teléfono bloqueado: tras decir «Confirmado», debe aparecer la pantalla normal de bloqueo; desbloquear manualmente y verificar si abre WhatsApp.
4. Responder «no» y comprobar que no se abra WhatsApp.

Si la prueba bloqueada no funciona, conservar el APK V1.5.3 y revisar los registros antes de otros cambios.

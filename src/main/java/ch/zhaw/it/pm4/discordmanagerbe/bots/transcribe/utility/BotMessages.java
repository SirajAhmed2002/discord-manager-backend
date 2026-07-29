package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized storage for all bot message templates with placeholder support.
 */
public class BotMessages {
    private static final Map<MessageKey, String> messages = new HashMap<>();

    static {
        // general messages
        messages.put(MessageKey.COMMAND_NOT_AVAILABLE, """
                **Dieser Befehl ist im aktuellen Bot-Zustand nicht verfügbar.**
                :bulb: Du kannst `/help` verwenden, um eine Liste der verfügbaren Befehle zu sehen.""");

        messages.put(MessageKey.PERMISSION_DENIED, "Nur der Bot-Owner kann diesen Befehl ausführen.");

        // Voice Channel messages
        messages.put(MessageKey.JOIN_SUCCESS, """
                :white_check_mark: **Erfolgreich verbunden mit Voice-Channel:**
                
                **Nächste Schritte:**
                :lock: `/lock-channel` - Sperre den Channel vor dem Aufnehmen
                :wave: `/leave-channel` - Verlasse den Channel""");

        messages.put(MessageKey.ALREADY_CONNECTED, """
                :information_source: Der Bot ist bereits mit einem Voice-Channel verbunden.
                Nutze `/lock-channel`, um den Channel zu sperren und mit der Aufnahme zu beginnen.""");

        messages.put(MessageKey.LEAVE_SUCCESS, """
                :wave: **Bot hat den Voice-Channel verlassen.**
                Verwende `/join-channel`, um erneut zu verbinden.""");

        messages.put(MessageKey.NOT_IN_VOICE_CHANNEL, """
                **Du musst in einem Voice-Channel sein, um diesen Befehl zu nutzen.**""");

        messages.put(MessageKey.CONNECTION_ERROR, """
                :x: **Verbindungsfehler:** %s
                Bitte überprüfe, ob der Bot die notwendigen Berechtigungen hat.""");

        // Channel Lock messages
        messages.put(MessageKey.CHANNEL_LOCKED, """
                :lock: **Voice-Channel wurde gesperrt!**
                Nur Administratoren können jetzt beitreten.
                
                **Nächste Schritte:**
                :white_check_mark: `/accept-recording` - Alle Teilnehmer müssen der Aufnahme zustimmen
                     • Sie akzeptieren, dass Ihre Stimme aufgenommen wird
                     • Sie akzeptieren, dass die Aufnahme mit ChatGPT verarbeitet wird
                :record_button: `/start-recording` - Starte die Aufnahme (erst möglich, wenn alle Teilnehmer zugestimmt haben)
                :unlock: `/unlock-channel` - Hebe die Sperrung auf""");

        messages.put(MessageKey.CHANNEL_UNLOCKED, """
                :unlock: **Voice-Channel wurde entsperrt!**
                Jeder kann wieder beitreten. Verwende `/lock-channel`, um den Channel erneut zu sperren.""");

        messages.put(MessageKey.CHANNEL_LOCKED_APPROVAL_NEEDED, """
                :lock: **Voice-Channel wurde gesperrt!** :microphone2: **Aufnahmeerlaubnis erforderlich**
                
                Der Channel ist nun gesichert - nur aktuelle Teilnehmer können bleiben.
                
                :warning: **Wichtig:** Alle Teilnehmer müssen nun der Aufnahme zustimmen:
                :white_check_mark: `/accept-recording` - Bestätige, dass
                     • Deine Stimme aufgenommen werden darf
                     • Die Aufnahme mit ChatGPT verarbeitet werden darf
                
                :information_source: Warte auf Bestätigung von: **%s**
                
                **Nach allen Zustimmungen:**
                :record_button: `/start-recording` - Aufnahme starten
                :unlock: `/unlock-channel` - Sperre aufheben & Anfrage abbrechen
                
                _Hinweis: Die Aufnahme kann erst gestartet werden, wenn alle zugestimmt haben._""");

        messages.put(MessageKey.ALREADY_LOCKED, """
                :information_source: Der Voice-Channel ist bereits gesperrt.
                Verwende `/start-recording`, um die Aufnahme zu beginnen oder `/unlock-channel`, um die Sperre aufzuheben.""");

        messages.put(MessageKey.CHANNEL_UNLOCK_WITH_CANCEL, """
                :unlock: **Voice-Channel wurde entsperrt!**
                Jeder kann wieder beitreten.
                :information_source: Die Aufnahmeanfrage wurde abgebrochen.""");

        messages.put(MessageKey.JOIN_FIRST, """
                :warning: Der Bot muss zuerst mit einem Voice-Channel verbunden sein.
                Verwende `/join-channel` während du in einem Voice-Channel bist.""");

        // recording messages
        messages.put(MessageKey.RECORDING_PERMISSION_REQUIRED, """
                :microphone2: **Aufnahmeerlaubnis erforderlich**
                
                Bitte bestätigt die Aufnahme mit `/accept-recording`
                Warte auf Bestätigung von: **%s**
                
                _Hinweis: Die Aufnahme kann erst gestartet werden, wenn alle Teilnehmer zugestimmt haben._""");

        messages.put(MessageKey.RECORDING_ACCEPTED, """
                :white_check_mark: **Aufnahme akzeptiert von:**""");

        messages.put(MessageKey.RECORDING_STARTED, """
                :red_circle: **Aufnahme gestartet:**
                
                :information_source: Benutzer-Audio wird einzeln aufgezeichnet.
                :stop_button: Beende mit `/stop-recording`
                
                _Hinweis: Während der Aufnahme können keine weiteren Personen dem Channel beitreten._""");

        messages.put(MessageKey.RECORDING_STOPPED, """
                :stop_button: **Aufnahme beendet**
                Die Aufnahme wird nun transkribiert.
                Verwende `/start-recording`, um eine neue Aufnahme zu starten, oder `/unlock-channel`, um den Channel zu entsperren.""");

        messages.put(MessageKey.RECORDING_ACTIVE, """
                :warning: Es läuft bereits eine Aufnahme.
                Beende diese zuerst mit `/stop-recording`.""");

        messages.put(MessageKey.NO_ACTIVE_RECORDING, """
                :information_source: Es gibt keine aktive Aufnahme in diesem Channel.
                Starte eine neue Aufnahme mit `/start-recording`.""");

        messages.put(MessageKey.LOCK_FIRST, """
                :warning: **Sicherheitshinweis:** Der Voice-Channel muss zuerst mit `/lock-channel` gesperrt werden, bevor die Aufnahme gestartet werden kann.
                Dies ist zum Schutz aller Teilnehmer und verhindert, dass unerwünschte Personen während der Aufnahme beitreten.""");

        messages.put(MessageKey.NO_PENDING_REQUEST, """
                :warning: Es gibt keine aktive Aufnahmeanfrage für diesen Channel.
                Starte eine neue Anfrage mit `/start-recording`.""");

        messages.put(MessageKey.ALL_APPROVED, """
                
                :tada: **Alle Benutzer haben der Aufnahme zugestimmt.**
                Die Aufnahme kann jetzt mit `/start-recording` gestartet werden.""");

        messages.put(MessageKey.WAITING_FOR_APPROVAL, """
                
                :hourglass: **Warte noch auf Zustimmung von:**""");

        // help messages
        messages.put(MessageKey.HELP_MESSAGE, """
                # **TranscribeBot Befehle**
                
                ## Grundlegende Befehle
                :speaker: `/join-channel` - Bot mit deinem aktuellen Voice-Channel verbinden
                :wave: `/leave-channel` - Bot vom Voice-Channel trennen
                
                ## Aufnahme-Befehle
                :lock: `/lock-channel` - Channel sperren (notwendig vor der Aufnahme)
                :unlock: `/unlock-channel` - Channel entsperren
                :record_button: `/start-recording` - Aufnahme starten (Zustimmung aller Teilnehmer erforderlich)
                :stop_button: `/stop-recording` - Aufnahme beenden und Dateien speichern
                
                ## Teilnehmer-Befehle
                :white_check_mark: `/accept-recording` - Der Aufnahme zustimmen
                
                **Hinweis:** Die meisten Befehle können nur vom Bot-Owner ausgeführt werden, der beim ersten `/join-channel` festgelegt wird.""");
    }

    /**
     * Returns a formatted message for the specified key.
     *
     * @param key The message key to retrieve
     * @param args Arguments for message placeholders
     * @return The formatted message
     */
    public static String get(MessageKey key, Object... args) {
        String message = messages.getOrDefault(key, "");
        if (args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }
}

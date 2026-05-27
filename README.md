# 🌐 Mindustry AutoTranslate Mod

Un mod client per **Mindustry** che traduce la chat di gioco in tempo reale utilizzando modelli linguistici locali tramite **Ollama** (`translategemma`). Il mod adatta automaticamente lo stile formale traducendo con lo slang tipico dei videogiocatori (*Gamer-to-Gamer Localization*).

---

## ✨ Caratteristiche principali

* **Traduzione Istantanea in Locale:** Sfrutta la potenza della tua GPU/CPU senza inviare dati a servizi esterni o API a pagamento.
* **Adattamento del Tono (Gamer Mode):** Dimentica le traduzioni robotiche da dizionario. Il mod traduce frasi complesse e slang mantenendo il contesto multigiocatore (es. trasforma frasi colloquiali in espressioni fluide come *"u a pro?"*, *"what the heck"*, *"кто в теме?"*).
* **Modalità Bidirezionale:**
  * **Messaggi degli altri giocatori:** Traduzione automatica dal Russo all'Inglese visibile solo a te sul tuo client.
  * **I tuoi messaggi:** Anteponendo il carattere `+` al tuo messaggio (es. `+hello boys`), la mod lo tradurrà in tempo reale in Russo informale e lo invierà automaticamente nella chat pubblica del server per gli altri giocatori.

---

## 🛠️ Come Funziona (Flusso Logico)

1. **Ascolto degli Eventi:** Il mod intercetta l'evento globale `PlayerChatEvent`.
2. **Controllo del Mittente:**
   * Se il messaggio è tuo e inizia con `+`, rimuove il prefisso, lo invia a Ollama per la traduzione verso il russo e usa `Call.sendChatMessage` per inviarlo in chiaro sul server.
   * Se il messaggio è di un altro giocatore, invia il testo a Ollama per la traduzione verso l'inglese e lo mostra localmente in chat sul tuo client tramite `Vars.player.sendMessage` (evidenziato in `[green]`).
3. **Asincronia Totale:** La richiesta HTTP ad Ollama viene eseguita su un thread separato (`Threads.thread`), evitando qualsiasi tipo di lag o micro-scatto al frame rate di gioco durante l'attesa dell'inferenza.

---

## 🚀 Requisiti e Installazione

### 1. Prerequisiti
È necessario che **Ollama** sia installato e in esecuzione sulla tua macchina locale.

1. Scarica Ollama da [ollama.com](https://ollama.com).
2. Scarica il modello ottimizzato per le traduzioni eseguendo questo comando nel terminale:
   ```bash
   ollama run translategemma

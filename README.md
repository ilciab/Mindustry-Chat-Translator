# 🌐 Mindustry AutoTranslate Mod

A client-side mod for **Mindustry** that translates in-game chat in real-time using local Large Language Models (LLMs) via **Ollama** (`translategemma`). The mod bypasses stiff, robotic translations by automatically adapting the text into natural gaming slang (*Gamer-to-Gamer Localization*).

---

## ✨ Key Features

* **Instant Local Translation:** Leverages your own GPU/CPU power without sending any data to external services or relying on paid API keys.
* **Tone Adaptation (Gamer Mode):** Say goodbye to rigid dictionary translations. The mod interprets complex sentences and gaming slang to maintain a natural multiplayer vibe (e.g., adapting phrases into smooth expressions like *"u a pro?"*, *"what the heck"*, or *"кто в теме?"*).
* **Bi-directional Mode:**
  * **Other players' messages:** Automatically translates incoming Russian messages into English, visible only to you on your client.
  * **Your messages:** By prefixing your message with a `+` (e.g., `+hello boys`), the mod translates it into informal Russian in real-time and automatically broadcasts it to the public server chat for other players.

---

## 🛠️ How It Works (Logical Flow)

1. **Event Listening:** The mod hooks into Mindustry's global `PlayerChatEvent`.
2. **Sender Validation:**
   * If the message is yours and starts with `+`, it strips the prefix, sends it to Ollama for an English-to-Russian translation, and triggers `Call.sendChatMessage` to send it publicly to the server.
   * If the message is from another player, it forwards the text to Ollama for a Russian-to-English translation and prints it locally in your chat window using `Vars.player.sendMessage` (highlighted in `[green]`).
3. **Fully Asynchronous:** The HTTP request to Ollama runs on a dedicated background thread (`Threads.thread`). This prevents any engine stutter, frame drops, or micro-lag while waiting for the AI model's inference.

---

## 🚀 Requirements & Installation

### 1. Prerequisites
You must have **Ollama** installed and running on your local machine.

1. Download Ollama from [ollama.com](https://ollama.com).
2. Download the model optimized for translation by running the following command in your terminal:
   ```bash
   ollama run translategemma

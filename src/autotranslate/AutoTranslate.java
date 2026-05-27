package autotranslate;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Threads;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call; // <-- Importante per inviare i messaggi al server
import mindustry.mod.Mod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AutoTranslate extends Mod {

    @Override
    public void init() {
        Events.on(EventType.ClientLoadEvent.class, e -> {
            Log.info("[AutoTranslate] Mod caricata e pronta!");
        });

        Events.on(EventType.PlayerChatEvent.class, e -> {
            if (e.message == null || e.message.trim().isEmpty()) return;

            String cleanMessage = Strings.stripColors(e.message).trim();

            boolean isMyMessage = (e.player != null && e.player == Vars.player);
            boolean isTranslationCommand = isMyMessage && cleanMessage.startsWith("+");

            if (isMyMessage && !isTranslationCommand) {
                return;
            }

            String textToTranslate = isTranslationCommand ? cleanMessage.substring(1).trim() : cleanMessage;
            if (textToTranslate.isEmpty()) return;

            Threads.thread(() -> {
                HttpURLConnection connection = null;
                try {
                    String systemPrompt;

                    if (isTranslationCommand) {
                        systemPrompt = String.format(
                                "ACT AS A GAMER. Translate the following text into Russian (informal). " +
                                        "Use gaming slang (like 'привет' instead of 'здравствуйте'). " +
                                        "Produce ONLY the translation, no quotes or commentary:\n\n\n%s",
                                textToTranslate
                        );
                    } else {
                        String sourceLang = "Russian";
                        String sourceCode = "ru";
                                                String targetLang = "English";
                        String targetCode = "en";

                        systemPrompt = String.format(
                                "You are an expert video game localizer and chat translator. Your goal is to translate casual gaming chat from %s (%s) to %s (%s). " +
                                        "Accurately convey the meaning, gaming slang, and informal nuances of the original text. Keep the tone natural, short, and appropriate for an in-game multiplayer chat (avoid overly formal or robotic language). " +
                                        "Produce only the translation, without any additional explanations or commentary. Translate this text:\n\n\n%s",
                                sourceLang, sourceCode, targetLang, targetCode, textToTranslate
                        );
                    }

                    Jval jsonReq = Jval.newObject();
                    jsonReq.put("model", "translategemma:latest");
                    jsonReq.put("prompt", systemPrompt);
                    jsonReq.put("stream", false);

                    String jsonBody = jsonReq.toString();
                    byte[] jsonBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

                    URL url = new URL("http://127.0.0.1:11434/api/generate");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");

                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(45000);
                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(jsonBytes.length);

                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(jsonBytes);
                        os.flush();
                    }

                    int code = connection.getResponseCode();

                    if (code == 200) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            String fullJson = response.toString();
                            Jval jsonRes = Jval.read(fullJson);
                            String translatedText = jsonRes.getString("response", "[Errore di traduzione]");

                            Core.app.post(() -> {
                                if (Vars.player == null) return;

                                if (isTranslationCommand) {
                                    Call.sendChatMessage(translatedText);
                                } else {
                                    String finalOutput;
                                    if (e.player != null) {
                                        finalOutput = "[" + Strings.stripColors(e.player.name()) + "]: [green]" + translatedText;
                                    } else {
                                        finalOutput = "[green]" + translatedText;
                                    }
                                    Vars.player.sendMessage(finalOutput);
                                }
                            });
                        }
                    } else {
                        Core.app.post(() -> {
                            if(Vars.player != null) Vars.player.sendMessage("[red]Errore: Ollama ha risposto male (HTTP " + code + ")");
                        });
                    }
                } catch (Exception ex) {
                    Core.app.post(() -> {
                        if(Vars.player != null) Vars.player.sendMessage("[red]Errore Connessione AutoTranslate: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                    });
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });
        });
    }
}
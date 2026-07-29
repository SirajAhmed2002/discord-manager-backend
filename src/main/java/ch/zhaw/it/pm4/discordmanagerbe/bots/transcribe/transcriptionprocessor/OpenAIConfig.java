package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OpenAI API integration.
 * Maps application.properties entries with the 'openai' prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {

    /** API configuration settings. */
    private Api api = new Api();

    /** Transcription service configuration settings. */
    private Transcription transcription = new Transcription();

    /**
     * Configuration properties for the OpenAI API connection.
     */
    public static class Api {
        /** The OpenAI API base URL. */
        private String url;

        /** The OpenAI API authentication key. */
        private String key;

        /** @return the API base URL */
        public String getUrl() {
            return url;
        }

        /** @param url the API base URL */
        public void setUrl(String url) {
            this.url = url;
        }

        /** @return the API authentication key */
        public String getKey() {
            return key;
        }

        /** @param key the API authentication key */
        public void setKey(String key) {
            this.key = key;
        }
    }

    /**
     * Configuration properties for OpenAI transcription service.
     */
    public static class Transcription {

        /** The OpenAI model for transcription operations. */
        private String model;

        /** @return the transcription model name */
        public String getModel() {
            return model;
        }

        /** @param model the transcription model name */
        public void setModel(String model) {
            this.model = model;
        }
    }

    /** @return the API configuration */
    public Api getApi() {
        return api;
    }

    /** @param api the API configuration */
    public void setApi(Api api) {
        this.api = api;
    }

    /** @return the transcription configuration */
    public Transcription getTranscription() {
        return transcription;
    }

    /** @param transcription the transcription configuration */
    public void setTranscription(Transcription transcription) {
        this.transcription = transcription;
    }
}

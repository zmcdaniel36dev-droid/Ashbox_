package com.nullform.ashbox.data

/**
 * Represents a language model available in the application.
 * Using a sealed class allows for different types of models or additional metadata later.
 */
sealed class Model(
    val id: String,
    val name: String,
    val version: String,
    val description: String
) {
    // Example models
    data object Gemma2B : Model(
        id = "gemma-2b",
        name = "Gemma 2B",
        version = "2b-it-q4",
        description = "A lightweight, state-of-the-art open model from Google."
    )

    data object Mistral7B : Model(
        id = "mistral-7b",
        name = "Mistral 7B",
        version = "7b-instruct-v0.2.Q4_K_M",
        description = "A powerful and efficient model by Mistral AI, great for instruction-following."
    )

    data object TinyLlama : Model(
        id = "tinyllama",
        name = "TinyLlama",
        version = "1.1B-Chat-v1.0-q4",
        description = "A compact 1.1B parameter model, perfect for mobile and offline applications."
    )

    // Add more models here as needed
    // data object YourNewModel : Model(id = "your-new-model", name = "Your New Model", ...)
}

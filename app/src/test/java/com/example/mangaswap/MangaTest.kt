package com.example.mangaswap

import com.example.mangaswap.screens.Manga
import org.junit.Assert.assertEquals
import org.junit.Test

class MangaTest {

    @Test
    fun testMangaDefaultValues() {
        val manga = Manga()

        assertEquals("", manga.title)
        assertEquals(0, manga.volume)
        assertEquals("", manga.author)
        assertEquals(0, manga.condition)
        assertEquals("", manga.imageUrl)
        assertEquals("", manga.owner)
    }

    @Test
    fun testMangaCustomValues() {
        val manga = Manga(
            mangaId = "1",
            title = "Naruto",
            volume = 1,
            author = "Masashi Kishimoto",
            condition = 5,
            imageUrl = "http://example.com/image.jpg",
            owner = "user123",
            ownerName = "JohnDoe",
            latitude = 35.6762,
            longitude = 139.6503
        )

        assertEquals("Naruto", manga.title)
        assertEquals(1, manga.volume)
        assertEquals("Masashi Kishimoto", manga.author)
        assertEquals(5, manga.condition)
        assertEquals("http://example.com/image.jpg", manga.imageUrl)
        assertEquals("user123", manga.owner)
        assertEquals("JohnDoe", manga.ownerName)
    }
}
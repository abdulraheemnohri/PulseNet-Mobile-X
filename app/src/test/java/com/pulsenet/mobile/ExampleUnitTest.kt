package com.pulsenet.mobile

import org.junit.Test
import org.junit.Assert.*
import com.pulsenet.mobile.data.Post

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun post_initialization() {
        val post = Post("1", "Author", "Content", 12345L)
        assertEquals("1", post.id)
        assertEquals("Author", post.author)
        assertEquals("Content", post.content)
    }
}

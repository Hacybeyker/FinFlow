package com.hacybeyker.finflow.feature.transactions.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Test

class HeroCollapseStateTest {

    private val range = 100f

    private fun state() = HeroCollapseState(collapseRangePx = range)

    private fun HeroCollapseState.scrollBy(deltaY: Float): Offset =
        nestedScrollConnection.onPreScroll(Offset(0f, deltaY), NestedScrollSource.UserInput)

    @Test
    fun `scrolling up collapses the hero and consumes exactly that delta from the list`() {
        val state = state()

        val consumed = state.scrollBy(-40f)

        assertEquals(0.4f, state.fraction, 0.001f)
        assertEquals(-40f, consumed.y, 0.001f) // the list must not receive what the hero used
    }

    @Test
    fun `collapse clamps at the full range and passes the rest through to the list`() {
        val state = state()

        val consumed = state.scrollBy(-250f)

        assertEquals(1f, state.fraction, 0.001f)
        assertEquals(-100f, consumed.y, 0.001f) // only the range is consumed; the list scrolls the rest
    }

    @Test
    fun `the first downward scroll re-expands the hero anywhere in the list`() {
        val state = state()
        state.scrollBy(-100f) // fully collapsed, list position irrelevant

        val consumed = state.scrollBy(30f)

        assertEquals(0.7f, state.fraction, 0.001f)
        assertEquals(30f, consumed.y, 0.001f)
    }

    @Test
    fun `when fully expanded a downward scroll is not consumed (overscroll stays natural)`() {
        val state = state()

        val consumed = state.scrollBy(50f)

        assertEquals(0f, state.fraction, 0.001f)
        assertEquals(0f, consumed.y, 0.001f)
    }
}

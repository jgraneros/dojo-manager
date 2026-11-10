package com.senshin.dojo_manager.students.domain.model

import com.senshin.dojo_manager.students.domain.exception.InvalidRankException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RankTest {

    @Test
    fun `rango con nivel fuera de rango lanza InvalidRankException`() {
        assertFailsWith<InvalidRankException> {
            Rank(RankType.KYU, level = 9, color = BeltColor.WHITE, orderIndex = 1)
        }
        assertFailsWith<InvalidRankException> {
            Rank(RankType.KYU, level = 0, color = BeltColor.WHITE, orderIndex = 1)
        }
    }

    @Test
    fun `rango con orderIndex invalido lanza InvalidRankException`() {
        assertFailsWith<InvalidRankException> {
            Rank(RankType.DAN, level = 1, color = BeltColor.BLACK, orderIndex = 0)
        }
    }

    @Test
    fun `tipo de rango invalido lanza InvalidRankException con valores permitidos`() {
        val ex = assertFailsWith<InvalidRankException> { RankType.from("SHODAN") }

        assertTrue(ex.message!!.contains("KYU"))
        assertTrue(ex.message!!.contains("DAN"))
    }

    @Test
    fun `parsear tipo de rango tolera mayusculas y espacios`() {
        assertEquals(RankType.KYU, RankType.from("kyu "))
        assertEquals(RankType.DAN, RankType.from(" Dan"))
    }

    @Test
    fun `color de cinturon invalido lanza InvalidRankException`() {
        assertFailsWith<InvalidRankException> { BeltColor.from("PURPLE") }
    }

    @Test
    fun `parsear color de cinturon tolera mayusculas`() {
        assertEquals(BeltColor.SKYE_BLUE, BeltColor.from("skye_blue"))
    }
}

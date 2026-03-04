package com.fjdias.personalfinance

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDateStabilityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testDatePickerRemainsOpenOnClick() {
        // 1. Garantir que estamos na aba de Transações
        composeTestRule.onNodeWithText("Transações").performClick()

        // 2. Clicar no botão de adicionar (+)
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()

        // 3. Verificar se o diálogo de transação abriu
        composeTestRule.onNodeWithText("Nova Transação").assertIsDisplayed()

        // 4. Clicar no botão de selecionar Data
        // Correção: usando hasText com substring = true para encontrar o botão que contém "Data:"
        composeTestRule.onNode(hasText("Data:", substring = true)).performClick()

        // 5. Verificar se o DatePicker (calendário) está visível e PERMANECE visível
        // Procuramos por elementos comuns do DatePicker do Material 3
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }
}

package com.fjdias.personalfinance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YearlySummaryTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testYearFilterLogic() {
        // 1. Garantir que estamos na aba de Transações
        composeTestRule.onNodeWithText("Transações").performClick()

        // 2. Adicionar uma transação em 2023
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()
        composeTestRule.onNodeWithText("Título").performTextInput("Gasto 2023")
        composeTestRule.onNodeWithText("Valor (R$)").performTextInput("100")
        // Aqui o diálogo de data abriria, mas para o teste vamos assumir o padrão ou manipular via ViewModel se necessário.
        // Como o foco é o filtro de ano, vamos verificar se o filtro aparece.
        composeTestRule.onNodeWithText("Salvar").performClick()

        // 3. Verificar se a aba "Todos Anos" e os anos detectados aparecem
        composeTestRule.onNodeWithText("Todos Anos").assertIsDisplayed()
        
        // 4. Ir para Resumo
        composeTestRule.onNodeWithText("Resumo").performClick()
        
        // 5. Verificar se o filtro de ano continua visível no resumo
        composeTestRule.onNodeWithText("Todos Anos").assertIsDisplayed()
    }
}

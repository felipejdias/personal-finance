# Desenvolvimento: Personal Finance App

Este documento registra o estado atual do projeto e o roteiro de funcionalidades futuras.

## Estado Atual do Projeto

### 1. Modelo de Dados (`Transaction.kt` & `Category.kt`)
- **Transactions**: Armazena `date`, `title`, `amount` e `categoryName`. Suporta tanto importação via CSV quanto inserção manual.
- **Categories**: Entidade persistente que permite ao usuário criar, editar e excluir categorias personalizadas.
- Persistência com **Room** (Versão 5).

### 2. Banco de Dados e DAO
- **AppDatabase**: Inicialização robusta no `onOpen` para garantir categorias padrão, incluindo a categoria especial "Renda".
- **TransactionDao**: Métodos para inserção em massa, inserção individual, limpeza, atualização individual e atualização de nomes em cascata.

### 3. Lógica de Negócio (`FinanceViewModel.kt`)
- Gerencia a separação de fluxos de dados filtrados para diferentes finalidades na UI.
- Implementa inserção e edição manual de transações com suporte a Coroutines.
- Coordena a atualização global de categorias para manter a consistência dos dados.

### 4. Interface do Usuário (`MainActivity.kt`)
- **Navegação**: 4 abas (Transações, Resumo, Gastos, Categorias).
- **Gestão de Transações**: 
    - Botão "+" (FAB) para inserção manual.
    - Clique em qualquer transação abre o `TransactionDialog` para edição completa (Título, Valor, Data e Categoria).
- **Lógica de Resumo Estrita**: 
    - Considera **Entrada** apenas transações na categoria "Renda".
    - Todas as outras categorias são tratadas como **Saídas**, independentemente do sinal do valor no CSV.
- **Filtros Independentes**: 
    - Aba Transações: Busca + Mês + Categoria.
    - Abas Analíticas: Filtro apenas por Mês (Visão Geral).
- **Componentes Customizados**: Uso de `DatePickerDialog` estabilizado para seleção de datas sem interrupções.

---

## Próximas Funcionalidades (Backlog)

### Fase 1: Visualização
- [ ] **Gráfico de Pizza**: Implementar representação visual na aba de Gastos.
- [ ] **Exportação**: Opção para exportar os dados filtrados para CSV.

### Fase 2: Controle
- [ ] **Metas**: Definir limites de gastos por categoria.
- [ ] **Recorrência**: Opção de marcar um gasto manual como "Mensal".

---

## Como usar o Contexto
O usuário possui total soberania sobre a classificação dos dados. O app mescla automação (importação) com controle manual granular, garantindo que o saldo final seja reflexo direto da renda declarada versus os gastos categorizados.

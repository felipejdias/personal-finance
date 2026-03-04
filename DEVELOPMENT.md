# Desenvolvimento: Personal Finance App

Este documento registra o estado atual do projeto e o roteiro de funcionalidades futuras.

## Estado Atual do Projeto

### 1. Modelo de Dados (`Transaction.kt` & `Category.kt`)
- **Transactions**: Armazena `date`, `title`, `amount` e `categoryName`.
- **Categories**: Entidade persistente que permite ao usuário criar, editar e excluir categorias personalizadas.
- Suporte a persistência com **Room** (Migração para Versão 2).

### 2. Banco de Dados (`AppDatabase.kt`, `TransactionDao.kt`, `CategoryDao.kt`)
- **Room Database** com suporte a múltiplas tabelas.
- **Auto-Populate**: O banco é inicializado com categorias padrão (Alimentação, Transporte, Saúde, etc.) na criação.
- Operações assíncronas via Kotlin Coroutines.

### 3. Lógica de Negócio (`CsvReader.kt` & `FinanceViewModel.kt`)
- **CsvReader**: Mapeamento dinâmico de títulos para nomes de categorias.
- **FinanceViewModel**: 
    - Gerenciamento de fluxo de dados (`Flow`) para transações e categorias.
    - Funções para CRUD de categorias e atualização de categoria de transação individual.

### 4. Interface do Usuário (`MainActivity.kt`)
- **Navegação**: `NavigationBar` com 4 abas (Transações, Resumo, Gastos, Categorias).
- **Sistema de Filtros**: 
    - Busca por texto.
    - Filtro por Mês (Tabs dinâmicas).
    - Filtro por Categoria (Abas dinâmicas baseadas nas categorias do usuário).
- **Gerenciamento de Transações**: Menu suspenso (`DropdownMenu`) em cada transação para troca rápida de categoria.
- **Tela de Categorias**: Interface para gerenciar o nome e a existência das categorias.

---

## Próximas Funcionalidades (Backlog)

### Fase 1: Refinamento e UX
- [ ] **Gráficos Visuais**: Substituir a lista de gastos por um gráfico de pizza (Pie Chart).
- [ ] **Exportação**: Opção para exportar os dados filtrados de volta para CSV.

### Fase 2: Gestão Financeira
- [ ] **Metas Mensais**: Definir orçamentos por categoria e exibir progresso.
- [ ] **Transações Manuais**: Adicionar um botão "+" para inserir gastos que não vieram do CSV.

---

## Como usar o Contexto
O app agora é totalmente dinâmico. O usuário tem controle total não apenas sobre os dados das transações, mas também sobre como elas são classificadas e organizadas através do sistema de categorias customizáveis.

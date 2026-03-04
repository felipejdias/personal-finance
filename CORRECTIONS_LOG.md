# Log de Alterações e Correções - Personal Finance

## [Recente] Implementação de Categorias Dinâmicas e Navegação
- **Categorias Mutáveis:**
    - Substituição do `enum Category` por uma entidade Room `@Entity Category`.
    - As categorias agora são armazenadas no banco de dados, permitindo Criação, Edição e Exclusão pelo usuário.
    - Inicialização do banco com categorias padrão (Alimentação, Transporte, Lazer, Saúde, Educação, Outros).
- **Gerenciamento de Transações:**
    - Adicionado suporte para alterar a categoria de uma transação individualmente através de um `DropdownMenu` na lista de transações.
    - Atualização do `CsvReader` para mapear títulos para os nomes das novas categorias dinâmicas.
- **Interface e Navegação:**
    - Introdução de uma `NavigationBar` com 4 seções: Transações, Resumo, Gastos (Gráfico/Breakdown) e Configurações de Categorias.
    - Implementação de filtros combinados: Busca por texto + Filtro por Mês + Filtro por Categoria.
    - Nova tela `CategoriesScreen` para gestão completa das categorias.

## [Anterior] Build Error - Kotlin Plugin
- **Problema:** Erro `Cannot convert the provided notation to an object of type Dependency: com.android.application:9.0.1`.
- **Causa:** Versão do AGP (Android Gradle Plugin) incorreta (`9.0.1`) e declaração de plugins via `classpath` em vez de `alias` no bloco `plugins`.
- **Solução:** 
    - Atualização do AGP para a versão estável `8.7.3` no `libs.versions.toml`.
    - Migração completa para a estrutura de `plugins { alias(...) }` tanto no root quanto no módulo `:app`.
    - Remoção de blocos `allprojects` e `buildscript` redundantes para evitar conflitos com `settings.gradle.kts`.

## Status Atual
- Projeto compilando e sincronizando com sucesso.
- Funcionalidades de Finanças operacionais com persistência em Room.

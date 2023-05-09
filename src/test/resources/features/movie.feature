# language: pt

Funcionalidade: Gerenciamento de filmes

Cenario: Listando os filmes
 Dado uma lista vazia de filmes
 Quando realiza uma busca de todos os filmes
 Entao a lista de filmes eh exibida

Esquema do Cenario: Realizando a busca de filme pelo ID
 Dado o ID <id> do filme
 Quando realiza uma busca do filme pelo ID
 Entao o filme eh exibido

Exemplos:
    | id |
    | 1  |
    | 2  |
    | 3  |
    | 4  |
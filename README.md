# web-scraping-scripts
Scripts para contar as skills mais exigidas em vagas para desenvolvedores nos sites da Catho e Glassdoor.

No arquivo _search_terms.txt_ são inseridos as skills, uma por linha, que serão contados pelo script.

Usar o caracter pipe ("|") para separar os termos alternativos e contar todos como um único skill.

Exemplo: _*NodeJS|Node.JS|Node JS*_

Usar o caracter asterisco ("*") após os termos que não queira que seja contado quando aparecer dentro de uma outra palavra similar. 

Exemplo: se inserir a skill _*Java**_ (com asterisco), a palavra _JavaScript_ na descrição de alguma vaga não será contabilizada na skill _*Java**_.

No método _main_ da classe *App* são executados os scripts. Descomentar o script que queria executar (Glassdoor ou Catho). E defina os valores desejados para os parâmetros _inputFilePath_, _outputFilePath_ e _searchKeyword_.

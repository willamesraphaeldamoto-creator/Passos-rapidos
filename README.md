# Passos Rápidos — Android

Aplicativo Android nativo inicial com contador de passos pelo sensor do aparelho, player para listas M3U8 e tela para copiar a chave PIX informada.

## Gerar o APK
1. Abra a aba **Actions** deste repositório.
2. Execute **Build Passos Rápidos APK** ou envie um commit para a branch main.
3. Ao concluir, abra a execução e baixe o artefato **Passos-Rapidos-debug**.

## Observações
- A contagem depende de sensor de passos e permissão de atividade física.
- As listas M3U8 são fontes externas; utilize apenas conteúdos que você tenha autorização para distribuir.
- A chave PIX é exibida para cópia. Esta versão não verifica pagamentos, não mantém saldo financeiro real e não realiza saques.
- Login remoto, banco de dados, painel administrativo, indicações com validação e pagamentos automáticos exigem backend seguro.
- Nunca coloque segredos de API dentro do APK.

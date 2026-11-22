# Bengali starter corpus: dataset card

## Provenance
All text in `corpus.txt` is short, manually authored Bengali educational prose and question-answer examples created for this repository. It is not scraped from books, news sites, Wikipedia, or private conversations. The repository maintainers may license these original contributions under CC0-1.0; contributors must only submit text they have the right to contribute.

## Intended use
A tiny reproducible experiment for checking a from-scratch character-level language-model training pipeline. It is not a broad Bengali knowledge corpus and is not suitable as the sole data source for a production assistant.

## Quality controls
- Manually authored in standard written Bengali.
- Includes basic language, arithmetic, science, geography and polite conversation.
- Keep held-out evaluation examples outside the training corpus.
- Review factual statements and spelling before accepting new contributions.
- Remove duplicates, unsupported claims, personal data and copyrighted text without permission.

## Limitations
This starter set is very small and has not been independently audited by professional Bengali editors. “Pure” or “error-free” cannot be guaranteed. It does not represent all Bengali dialects, registers or communities. A model trained only on it is expected to overfit and produce poor answers.

## License
CC0-1.0 is intended for the original repository-authored examples. Any future third-party data must have its own recorded source, license and attribution requirements before inclusion.

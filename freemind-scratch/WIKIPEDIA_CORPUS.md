# Complete Bengali Wikipedia corpus preparation

This workflow downloads and streams the complete Bengali subset (`bn`) from
`HuggingFaceFW/finewiki` into a UTF-8 text corpus. The dataset card identifies
the Bengali subset as 187,386 pages / 988.8 MB (August 2025 snapshot).

## Run

GitHub → Actions → **Prepare complete Bengali Wikipedia corpus** → Run workflow.

The workflow checks that all 187,386 expected articles were written and uploads
the corpus plus a JSON manifest as an artifact. It does **not** claim that model
training has happened. The artifact is the complete corpus input for the next
training stage.

## Attribution and license

FineWiki's source text is from Wikipedia and is licensed CC BY-SA 4.0 (also
available under GFDL). Preserve attribution to Wikipedia contributors and comply
with the share-alike terms when distributing adapted corpus/model materials.
Dataset: https://huggingface.co/datasets/HuggingFaceFW/finewiki

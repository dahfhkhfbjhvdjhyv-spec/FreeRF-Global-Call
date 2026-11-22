# SmartTalk AI — বাংলা অফলাইন AI গবেষণা

এই ফোল্ডারটি SmartTalk AI-এর স্বতন্ত্র বাংলা মডেল পরীক্ষার অংশ। FreeRF Bluetooth কল অ্যাপের সঙ্গে এর কোনো কার্যকরী নির্ভরতা নেই।

## বর্তমান পর্যায়
- `corpus.txt`: নিজস্বভাবে লেখা ছোট বাংলা প্রশিক্ষণ-নমুনা।
- `train.py`: এলোমেলো ওজন থেকে ছোট character-level Transformer প্রশিক্ষণ।
- `eval.txt` ও `evaluate.py`: প্রশিক্ষণে ব্যবহার না করা বাংলা প্রশ্নে পরবর্তী-অক্ষর prediction মূল্যায়ন।
- `.github/workflows/smarttalk-train.yml`: GitHub Actions-এ CPU training, evaluation ও নমুনা generation।

## চালানো
```bash
pip install -r requirements.txt
python train.py --steps 300 --out model-output
python evaluate.py --checkpoint model-output/model.pt
python generate.py --checkpoint model-output/model.pt --prompt "বাংলা ভাষা" --tokens 80
```

## গুরুত্বপূর্ণ সীমাবদ্ধতা
এটি শূন্য থেকে প্রশিক্ষিত ক্ষুদ্র গবেষণা মডেল; কথোপকথন-সহকারী বা মানুষের মতো বুদ্ধিমান মডেল নয়। ছোট ডেটাসেটে overfit হওয়ার আশঙ্কা প্রবল। Evaluation-এর perplexity বা character coverage কখনোই ৯৫% সঠিক উত্তর দেওয়ার প্রমাণ নয়। বাস্তব ব্যবহারকারীর প্রশ্নে মানুষের তৈরি আলাদা benchmark, factuality review এবং বহু ডিভাইসে পরীক্ষা ছাড়া Android অ্যাপে যুক্ত করা হবে না।

ডেটাসেটের উৎস, অনুমিত লাইসেন্স ও সীমাবদ্ধতা `DATASET_CARD.md`-এ নথিভুক্ত।

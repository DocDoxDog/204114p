import cloudscraper
from bs4 import BeautifulSoup
import csv

scraper = cloudscraper.create_scraper()
url = "https://chulatutor.com/blog/คำศัพท์-ielts/"
response = scraper.get(url)

if response.status_code == 200:
    soup = BeautifulSoup(response.text, "html.parser")
    
    # 1. Locate the table (adjust if the table has class or id attributes)
    table = soup.find("table")

    # 2. If you find the table, get all rows
    #    Some tables might not have <tbody>, so adapt if necessary
    rows = table.find("tbody").find_all("tr")

    # 3. Extract word + meaning from each row
    vocab_data = []
    for row in rows:
        cells = row.find_all("td")
        if len(cells) >= 2:
            word = cells[0].get_text(strip=True)
            meaning = cells[1].get_text(strip=True)
            vocab_data.append((word, meaning))

    # 4. Write to CSV
    with open("vocab.csv", mode="w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["Word", "Meaning"])  # header row
        writer.writerows(vocab_data)

    print("Scraping complete. Check vocab.csv")

else:
    print("Failed to get the page:", response.status_code)

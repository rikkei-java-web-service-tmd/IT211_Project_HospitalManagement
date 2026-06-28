import pandas as pd
import docx
import sys

def read_excel():
    print("--- EXCEL CONTENT ---")
    xls = pd.ExcelFile('Bảng điểm Project Java Web Service Bệnh Viện.xlsx')
    for sheet in xls.sheet_names:
        print(f"\nSheet: {sheet}")
        df = pd.read_excel(xls, sheet_name=sheet)
        print(df.to_string())

def read_docx():
    print("\n--- DOCX CONTENT ---")
    doc = docx.Document('Hệ thống quản lý bệnh viện (Java Web Service cung cấp RESTful API).docx')
    for para in doc.paragraphs:
        if para.text.strip():
            print(para.text)

if __name__ == '__main__':
    with open('output_docs.txt', 'w', encoding='utf-8') as f:
        sys.stdout = f
        read_excel()
        read_docx()
    print("Done writing to output_docs.txt")

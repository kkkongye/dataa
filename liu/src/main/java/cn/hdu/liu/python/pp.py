import pandas as pd
import re

# 读取 Excel 文件
df = pd.read_excel(r"J:\excelll\基本登记信息模拟数据(1).xlsx")

# 输出文件路径
output_file =  r"J:\excelll\data_issues.txt"

with open(output_file, "w", encoding="utf-8") as f:
    for idx, row in df.iterrows():
        row_num = idx + 2  # Excel 中第1行为表头，从第2行开始是数据

        for col in df.columns:
            value = row.get(col)
            value_str = str(value).strip()

            # 缺失值判断
            if pd.isnull(value) or value_str == "":
                f.write(f"[第{row_num}行] [{col}] 缺失\n")

            # 出生日期字段的数值合法性判断
            if col == "出生日期" and not pd.isnull(value) and value_str != "":
                numbers = re.findall(r'\d+', value_str)
                numbers = list(map(int, numbers))
                if len(numbers) < 3:
                    f.write(f"[第{row_num}行] 出生日期信息不足：{value_str}\n")
                else:
                    year, month, day = numbers[:3]
                    if year > 2025 or year < 1900:
                        f.write(f"[第{row_num}行] 出生年份异常：{year}\n")
                    if month < 1 or month > 12:
                        f.write(f"[第{row_num}行] 出生月份异常：{month}\n")
                    if day < 1 or day > 31:
                        f.write(f"[第{row_num}行] 出生日期值异常：{day}\n")

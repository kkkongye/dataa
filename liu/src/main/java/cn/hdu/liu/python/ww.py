import pandas as pd
import re
from datetime import datetime

# 读取 Excel 表格
file_path = r"J:\excelll\身份认证信息1(1).xlsx"
df = pd.read_excel(file_path)

# 标准字段名列表
columns = [
    '姓名', '性别', '身份证', '出生地', '住址', '公安机关',
    '街路巷', '居委会', '城乡属性', '服务处所', '有效期'
]
df = df[columns]

# 异常与缺失信息收集列表
issues = []

# 缺失值检测
for idx, row in df.iterrows():
    for col in columns:
        if pd.isnull(row[col]) or str(row[col]).strip() == '':
            issues.append(f"第 {idx+1} 条数据 缺失字段：{col}")

# 纯数字异常值检测（仅针对文字类字段）
text_fields = ['姓名', '出生地', '住址', '公安机关', '街路巷', '居委会', '服务处所']
for idx, row in df.iterrows():
    for field in text_fields:
        val = str(row[field]).strip()
        if re.fullmatch(r'\d+', val):
            issues.append(f"第 {idx+1} 条数据 字段“{field}”为纯数字：{val}")

# 身份证校验函数
def validate_id_card(id_card):
    if not isinstance(id_card, str):
        return "身份证格式错误（非字符串）"
    id_card = id_card.strip().upper()
    if len(id_card) != 18:
        return f"身份证长度错误：{len(id_card)}位"
    if not re.match(r'^\d{17}[\dX]$', id_card):
        return "身份证格式不合法（应为17位数字+1位校验码）"

    # 校验码计算
    weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    check_codes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    try:
        total = sum(int(id_card[i]) * weights[i] for i in range(17))
        if id_card[-1] != check_codes[total % 11]:
            return "身份证校验码错误"
    except:
        return "身份证内容异常（包含非法字符）"
    return None  # 合法

# 执行身份证校验
for idx, row in df.iterrows():
    id_card = str(row['身份证']).strip()
    result = validate_id_card(id_card)
    if result:
        issues.append(f"第 {idx+1} 条数据 身份证异常：{id_card}，原因：{result}")

# 有效期合法性校验
def is_valid_date(date_str):
    try:
        date_obj = datetime.strptime(date_str, '%Y-%m-%d')
        if 1 <= date_obj.month <= 12 and 1 <= date_obj.day <= 31:
            return True
    except:
        return False
    return False

for idx, row in df.iterrows():
    val = str(row['有效期']).strip()
    match = re.search(r'(\d{4}-\d{2}-\d{2})\s*至\s*(\d{4}-\d{2}-\d{2}|长期)', val)
    if not match:
        issues.append(f"第 {idx+1} 条数据 有效期格式异常：{val}")
    else:
        start, end = match.group(1), match.group(2)
        if not is_valid_date(start):
            issues.append(f"第 {idx+1} 条数据 有效期起始日期非法：{start}")
        if end != '长期' and not is_valid_date(end):
            issues.append(f"第 {idx+1} 条数据 有效期结束日期非法：{end}")

# 写入 TXT 报告
with open("J:\excelll\数据缺失与异常报告.txt", "w", encoding='utf-8') as f:
    if not issues:
        f.write("所有数据正常，无缺失或异常值。")
    else:
        for line in issues:
            f.write(line + "\n")

print(f" 检查完毕，共发现 {len(issues)} 项问题，已保存至 数据缺失与异常报告.txt")
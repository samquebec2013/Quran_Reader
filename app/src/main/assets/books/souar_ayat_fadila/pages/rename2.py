import os

prefix = "page"
extension = ".jpg"

# Rename files from 14 to 54 → shift down by 1
for i in range(14, 55):  # 55 because range end is exclusive
    old_name = f"{prefix}{i}.jpg"
    new_name = f"{prefix}{i - 1}.jpg"
    
    if os.path.exists(old_name):
        os.rename(old_name, new_name)
        print(f"Renamed {old_name} -> {new_name}")
    else:
        print(f"Skipped (not found): {old_name}")

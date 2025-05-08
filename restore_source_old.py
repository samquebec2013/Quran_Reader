import os
import re

def restore_files_from_backup(source_file):
    with open(source_file, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Split content based on file markers
    file_sections = re.split(r'Fichier: (.*?)\n', content)[1:]
    
    # Iterate over file sections
    for i in range(0, len(file_sections), 2):
        file_path = file_sections[i].strip()
        file_content = file_sections[i+1].strip()
        
        # Ensure the directory exists
        file_dir = os.path.dirname(file_path)
        os.makedirs(file_dir, exist_ok=True)
        
        # Write content to the file
        with open(file_path, 'w', encoding='utf-8') as out_file:
            out_file.write(file_content)
        
        print(f'Restored: {file_path}')

# Run the restoration process
restore_files_from_backup('code_source.txt')

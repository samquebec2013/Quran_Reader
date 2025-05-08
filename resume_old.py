import os
import re

def extract_kotlin_info(file_path):
    """Extrait les classes, objets et fonctions d'un fichier Kotlin"""
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()
    
    classes = re.findall(r'class\s+(\w+)', content)
    objects = re.findall(r'object\s+(\w+)', content)
    functions = re.findall(r'fun\s+(\w+)\(', content)
    
    return classes, objects, functions

def extract_xml_info(file_path):
    """Extrait les IDs des vues depuis un fichier XML (layout)"""
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()
    
    view_ids = re.findall(r'android:id="@\+id/(\w+)"', content)
    return view_ids

def scan_project(project_path):
    """Parcourt le projet Android et extrait les infos essentielles"""
    summary = []
    source_files = []
    
    for root, _, files in os.walk(project_path):
        for file in files:
            file_path = os.path.join(root, file)
            
            if file.endswith(('.kt', '.xml', '.json', '.txt')):
                source_files.append(file_path)
            
            if file.endswith('.kt'):
                classes, objects, functions = extract_kotlin_info(file_path)
                if classes or objects or functions:
                    summary.append(f'Fichier: {file_path}')
                    if classes:
                        summary.append(f'  Classes: {", ".join(classes)}')
                    if objects:
                        summary.append(f'  Objets: {", ".join(objects)}')
                    if functions:
                        summary.append(f'  Fonctions: {", ".join(functions)}')
                    summary.append('')
            
            elif file.endswith('.xml'):
                view_ids = extract_xml_info(file_path)
                summary.append(f'Fichier: {file_path}')
                if view_ids:
                    summary.append(f'  Vues (IDs): {", ".join(view_ids)}')
                summary.append('')
            
            elif file.endswith(('.png', '.jpg', '.webp', '.svg', '.mp3', '.wav', '.ttf')):
                summary.append(f'Fichier de ressource: {file_path}')
                summary.append('')
    
    return '\n'.join(summary), source_files

def concatenate_source_code(file_list, output_file):
    """Concatène le contenu de tous les fichiers source dans un seul fichier"""
    with open(output_file, 'w', encoding='utf-8') as out_file:
        for file_path in file_list:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                out_file.write(f'Fichier: {file_path}\n')
                out_file.write(f.read())
                out_file.write('\n')
    print(f'Code source concaténé enregistré dans {output_file}')

# Exécuter le script
project_root = 'app/src/'  # Modifier selon l'emplacement du projet
summary_text, source_files = scan_project(project_root)

# Enregistrer le résumé dans un fichier
output_summary = 'project_summary.txt'
with open(output_summary, 'w', encoding='utf-8') as f:
    f.write(summary_text)
print(f'Résumé enregistré dans {output_summary}')

# Concaténer le code source
output_code_file = 'code_source.txt'
concatenate_source_code(source_files, output_code_file)

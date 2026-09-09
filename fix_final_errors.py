import re

# Fix this.service = service; assignments
files_to_fix_assignment = [
    'src/main/java/com/logistics/packinglist/ui/ProcesarBODialog.java',
    'src/main/java/com/logistics/packinglist/ui/ReubicarStockDialog.java',
    'src/main/java/com/logistics/packinglist/ui/GestionTrackingDialog.java'
]
for filepath in files_to_fix_assignment:
    with open(filepath, 'r') as f:
        content = f.read()
    content = re.sub(r'this\.service\s*=\s*service\s*;\n?', '', content)
    with open(filepath, 'w') as f:
        f.write(content)

# Fix orderNoteApiService in ManualPackingDialog.java
with open('src/main/java/com/logistics/packinglist/ui/ManualPackingDialog.java', 'r') as f:
    content = f.read()
content = re.sub(r'\borderNoteApiService\b', 'service', content)
with open('src/main/java/com/logistics/packinglist/ui/ManualPackingDialog.java', 'w') as f:
    f.write(content)


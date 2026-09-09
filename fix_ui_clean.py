import glob
import re

for filepath in glob.glob('src/main/java/com/logistics/packinglist/ui/*.java'):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find ALL field names of type MantenimientoService
    # This matches: private [final] MantenimientoService fieldName [= ...];
    pattern = r'private\s+(?:final\s+)?MantenimientoService\s+([a-zA-Z0-9_]+)\s*(?:=\s*[^;]+)?;'
    field_names = set(re.findall(pattern, content))
    
    # Remove all their declarations
    content = re.sub(pattern + r'\n?', '', content)
    
    # Remove all their instantiations: e.g., this.service = new MantenimientoService();
    content = re.sub(r'this\.[a-zA-Z0-9_]+\s*=\s*new\s+MantenimientoService\(\)\s*;\n?', '', content)
    
    # Replace all usages of those fields with 'service'
    for fn in field_names:
        if fn != 'service':
            content = re.sub(r'\b' + fn + r'\b', 'service', content)

    # Remove any existing MantenimientoService service = MantenimientoService.getInstance();
    content = re.sub(r'private\s+(?:final\s+)?MantenimientoService\s+service\s*=\s*MantenimientoService\.getInstance\(\)\s*;\n?', '', content)

    # Insert a single 'private final MantenimientoService service = MantenimientoService.getInstance();'
    # after the class declaration
    content = re.sub(r'(public class [a-zA-Z0-9_]+(?: extends [a-zA-Z0-9_]+)?(?: implements [a-zA-Z0-9_, ]+)?\s*\{)', 
                     r'\1\n    private final MantenimientoService service = MantenimientoService.getInstance();\n', content)
                     
    with open(filepath, 'w') as f:
        f.write(content)


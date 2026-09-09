import glob
import re

for filepath in glob.glob('src/main/java/com/logistics/packinglist/ui/*.java'):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find all field names of type MantenimientoService
    field_names = set(re.findall(r'private\s+(?:final\s+)?MantenimientoService\s+([a-zA-Z0-9_]+)\s*;', content))
    
    # Remove all their declarations
    content = re.sub(r'private\s+(?:final\s+)?MantenimientoService\s+[a-zA-Z0-9_]+\s*;\n?', '', content)
    
    # Remove all their instantiations: e.g., this.service = new MantenimientoService();
    content = re.sub(r'this\.[a-zA-Z0-9_]+\s*=\s*new\s+MantenimientoService\(\)\s*;\n?', '', content)
    
    # Replace all usages of those fields with 'service'
    for fn in field_names:
        if fn != 'service':
            content = re.sub(r'\b' + fn + r'\b', 'service', content)

    # Insert a single 'private final MantenimientoService service = MantenimientoService.getInstance();'
    # after the class declaration
    content = re.sub(r'(public class [a-zA-Z0-9_]+(?: extends [a-zA-Z0-9_]+)?(?: implements [a-zA-Z0-9_, ]+)?\s*\{)', 
                     r'\1\n    private final MantenimientoService service = MantenimientoService.getInstance();\n', content)
                     
    # Clean up duplicate imports
    content = re.sub(r'(import com.logistics.packinglist.service.MantenimientoService;\n)+', 'import com.logistics.packinglist.service.MantenimientoService;\n', content)

    with open(filepath, 'w') as f:
        f.write(content)


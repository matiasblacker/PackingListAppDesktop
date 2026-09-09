import glob, re

services = glob.glob('src/main/java/com/logistics/packinglist/service/*ApiService.java')
services = [s for s in services if 'BaseApiService' not in s]

facade_code = """package com.logistics.packinglist.service;

import com.logistics.packinglist.model.*;
import java.util.List;

public class MantenimientoService {
"""

# Instantiate all services
for s in services:
    class_name = s.split('/')[-1].replace('.java', '')
    var_name = class_name[0].lower() + class_name[1:]
    facade_code += f"    private final {class_name} {var_name} = new {class_name}();\n"

facade_code += "\n"

# Extract public methods and delegate
for s in services:
    class_name = s.split('/')[-1].replace('.java', '')
    var_name = class_name[0].lower() + class_name[1:]
    
    with open(s, 'r') as f:
        content = f.read()
    
    # regex to find public methods. e.g. public List<CompanyModel> obtenerEmpresas() throws Exception
    # or public void eliminarEmpresa(String id) throws Exception
    methods = re.findall(r'public\s+(?:<[^>]+>\s+)?([\w<>\.]+)\s+(\w+)\s*\(([^)]*)\)\s*(?:throws\s+[\w,\s]+)?\s*\{', content)
    for ret_type, method_name, args in methods:
        # Extract argument names for delegation
        arg_names = []
        if args.strip():
            for arg in args.split(','):
                parts = arg.strip().split()
                if parts:
                    arg_names.append(parts[-1])
        args_str = ', '.join(arg_names)
        
        return_stmt = "return " if ret_type != 'void' else ""
        facade_code += f"    public {ret_type} {method_name}({args}) throws Exception {{\n"
        facade_code += f"        {return_stmt}{var_name}.{method_name}({args_str});\n"
        facade_code += f"    }}\n\n"

facade_code += "}\n"

with open('src/main/java/com/logistics/packinglist/service/MantenimientoService.java', 'w') as f:
    f.write(facade_code)

print("Facade generated")

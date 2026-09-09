import os, re

def replace_button(match, btn_type):
    full_declaration = match.group(1) # e.g. "Button btn" or "btn"
    text = match.group(2)
    var_name = full_declaration.split()[-1] # gets "btn"
    
    icon = ""
    if btn_type == "cancelar":
        icon = "TIMES"
    elif btn_type == "guardar":
        icon = "SAVE"
    elif btn_type == "limpiar":
        icon = "UNDO"
        
    res = f'{full_declaration} = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.{icon}));\n'
    res += f'        {var_name}.getStyleClass().add("btn-{btn_type}");'
    return res

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original = content
    
    # 1. Cancelar / Anular / Cerrar
    content = re.sub(
        r'(Button\s+[a-zA-Z0-9_]+|[a-zA-Z0-9_]+)\s*=\s*new\s+Button\s*\(\s*"(Cancelar|Anular Tracking|Cerrar)"[^)]*\)\s*;',
        lambda m: replace_button(m, "cancelar"),
        content
    )
    
    # 2. Guardar / Aceptar / Confirmar
    content = re.sub(
        r'(Button\s+[a-zA-Z0-9_]+|[a-zA-Z0-9_]+)\s*=\s*new\s+Button\s*\(\s*"(Guardar|Aceptar|Confirmar|Confirmar Despacho)"[^)]*\)\s*;',
        lambda m: replace_button(m, "guardar"),
        content
    )
    
    # 3. Limpiar
    content = re.sub(
        r'(Button\s+[a-zA-Z0-9_]+|[a-zA-Z0-9_]+)\s*=\s*new\s+Button\s*\(\s*"(Limpiar|Limpiar Formulario|Limpiar Filtros)"[^)]*\)\s*;',
        lambda m: replace_button(m, "limpiar"),
        content
    )
    
    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk("./src/main/java/com/logistics/packinglist/ui"):
    for f in files:
        if f.endswith(".java"):
            process_file(os.path.join(root, f))

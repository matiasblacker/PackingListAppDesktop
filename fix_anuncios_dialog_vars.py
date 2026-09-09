with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

target = "private FlowPane customFieldsPane;"
replacement = """private final List<ProductFieldDefinitionModel> activeDocFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> docCustomFieldInputMap = new HashMap<>();
    private HBox docCustomFieldsPane;
    private FlowPane customFieldsPane;"""

content = content.replace(target, replacement)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Fixed field declarations in AnunciosDialog.java!")

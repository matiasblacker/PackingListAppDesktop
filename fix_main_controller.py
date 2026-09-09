import re

with open('src/main/java/com/logistics/packinglist/ui/MainController.java', 'r') as f:
    content = f.read()

# Remove line 104: private final AuthService maintenanceService = new AuthService();
content = re.sub(r'private\s+final\s+AuthService\s+maintenanceService\s*=\s*new\s+AuthService\(\)\s*;\n?', '', content)

# Replace all occurrences of maintenanceService with service
content = re.sub(r'\bmaintenanceService\b', 'service', content)

# Also remove duplicate imports
content = re.sub(r'(import com.logistics.packinglist.service.AuthService;\n)+', 'import com.logistics.packinglist.service.AuthService;\n', content)

with open('src/main/java/com/logistics/packinglist/ui/MainController.java', 'w') as f:
    f.write(content)


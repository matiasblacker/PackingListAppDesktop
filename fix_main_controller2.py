import re

with open('src/main/java/com/logistics/packinglist/ui/MainController.java', 'r') as f:
    lines = f.readlines()

with open('src/main/java/com/logistics/packinglist/ui/MainController.java', 'w') as f:
    for line in lines:
        if 'AuthService service = new AuthService();' in line:
            continue
        f.write(line)


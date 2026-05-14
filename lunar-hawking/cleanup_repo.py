import sys

def cleanup_repository(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    new_lines = []
    in_static_eateries = False
    
    for line in lines:
        if 'private val staticEateries = listOf(' in line:
            in_static_eateries = True
            new_lines.append(line)
            # Add back only the original/main 4 eateries
            new_lines.append('        Eatery("1", "Basaveshwara Heritage Stay", "Home-stay", "Authentic Village Stay & North Karnataka Meals", "https://images.unsplash.com/photo-1598514982205-f36b96d1e8d4?q=80&w=500", 15.4590, 75.0080),\n')
            new_lines.append('        Eatery("2", "Pavithra Hill View Stay", "Home-stay", "Famous Thatte Idli & Valley View", "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=500", 13.3400, 77.1000),\n')
            new_lines.append('        Eatery("3", "Mishra Garden Retreat", "Home-stay", "Dharwad Pedha & Peaceful Garden Stay", "https://images.unsplash.com/photo-1540518614846-7eded433c457?q=80&w=500", 15.4500, 75.0100),\n')
            new_lines.append('        Eatery("4", "Malnad Heritage Stay", "Home-stay", "Akki Roti & Misty Mountain Coffee", "https://images.unsplash.com/photo-1598514982205-f36b96d1e8d4?q=80&w=500", 14.6200, 74.8450)\n')
            continue
        
        if in_static_eateries:
            if ')' in line and line.strip().endswith(')'):
                in_static_eateries = False
                new_lines.append(line)
            continue
        
        new_lines.append(line)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

cleanup_repository(r'C:\Users\krish\.gemini\antigravity\playground\lunar-hawking\app\src\main\java\com\example\santheconnect\data\MarketRepository.kt')
print("Homestay list cleaned up successfully")

// XM CLIENT MOBILE - Interactive Web Simulator Engine
// Author: Mayank Mandrai

const state = {
  version: '1.21.1',
  loader: 'fabric',
  installedMods: new Set(['sodium-fabric-1.21.1']), // pre-installed example
  installedFiles: new Set(['sodium.jar']),
  activeSource: 'ALL',
  selectedBtn: null,
  buttons: [
    { id: 'btn_w', label: 'W', x: 0.12, y: 0.58, w: 60, h: 60, key: 'FORWARD (W)', round: false },
    { id: 'btn_a', label: 'A', x: 0.05, y: 0.72, w: 60, h: 60, key: 'LEFT (A)', round: false },
    { id: 'btn_s', label: 'S', x: 0.12, y: 0.72, w: 60, h: 60, key: 'BACK (S)', round: false },
    { id: 'btn_d', label: 'D', x: 0.19, y: 0.72, w: 60, h: 60, key: 'RIGHT (D)', round: false },
    { id: 'btn_sprint', label: 'SPRINT', x: 0.12, y: 0.44, w: 68, h: 40, key: 'SPRINT (Ctrl)', round: false },
    { id: 'btn_sneak', label: 'SNEAK', x: 0.12, y: 0.86, w: 68, h: 40, key: 'SNEAK (Shift)', round: false },
    { id: 'btn_jump', label: 'JUMP', x: 0.88, y: 0.74, w: 72, h: 72, key: 'JUMP (Space)', round: true },
    { id: 'btn_attack', label: 'ATTACK', x: 0.78, y: 0.60, w: 76, h: 76, key: 'ATTACK (L-Click)', round: true },
    { id: 'btn_use', label: 'USE', x: 0.88, y: 0.48, w: 72, h: 72, key: 'USE (R-Click)', round: true },
    { id: 'btn_inv', label: 'INV', x: 0.50, y: 0.88, w: 70, h: 44, key: 'INVENTORY (E)', round: false },
    { id: 'btn_f3', label: 'F3', x: 0.05, y: 0.10, w: 48, h: 36, key: 'DEBUG (F3)', round: false },
    { id: 'btn_f5', label: 'F5', x: 0.12, y: 0.10, w: 48, h: 36, key: 'CAMERA (F5)', round: false },
    { id: 'btn_zoom', label: 'ZOOM', x: 0.19, y: 0.10, w: 56, h: 36, key: 'ZOOM (C)', round: false },
    { id: 'btn_chat', label: 'CHAT', x: 0.50, y: 0.10, w: 60, h: 36, key: 'CHAT (T)', round: false },
    { id: 'btn_pause', label: 'ESC', x: 0.94, y: 0.10, w: 48, h: 36, key: 'PAUSE (Esc)', round: false }
  ]
};

// Mod Catalogue Mock + Modrinth API Live Query
const sampleMods = [
  {
    id: 'sodium',
    slug: 'sodium',
    title: 'Sodium',
    author: 'jellysquid3_',
    source: 'Modrinth',
    downloads: '32,541,029',
    description: 'A modern rendering engine for Minecraft that greatly improves frame rates and reduces stutter.',
    icon: 'https://cdn.modrinth.com/data/AANobbMI/icon.png',
    dependencies: [
      { name: 'Fabric API', id: 'fabric-api', isInstalled: true, isRequired: true }
    ]
  },
  {
    id: 'iris',
    slug: 'iris',
    title: 'Iris Shaders',
    author: 'coderbot',
    source: 'Modrinth',
    downloads: '24,198,401',
    description: 'A modern shaders mod for Minecraft compatible with existing OptiFine shader packs.',
    icon: 'https://cdn.modrinth.com/data/YL57xq9U/icon.png',
    dependencies: [
      { name: 'Sodium', id: 'sodium', isInstalled: true, isRequired: true },
      { name: 'Fabric API', id: 'fabric-api', isInstalled: true, isRequired: true }
    ]
  },
  {
    id: 'jei',
    slug: 'jei',
    title: 'Just Enough Items (JEI)',
    author: 'mezz',
    source: 'CurseForge',
    downloads: '289,140,551',
    description: 'View Items and Recipes in Minecraft. The most essential recipe and utility mod.',
    icon: 'https://media.forgecdn.net/avatars/29/70/635838940866020556.png',
    dependencies: []
  },
  {
    id: 'ferritecore',
    slug: 'ferrite-core',
    title: 'FerriteCore',
    author: 'malte0811',
    source: 'Modrinth',
    downloads: '18,450,210',
    description: 'Memory usage optimizations for Minecraft. Drastically reduces RAM on mobile.',
    icon: 'https://cdn.modrinth.com/data/uXXizFIs/icon.png',
    dependencies: []
  },
  {
    id: 'cloth-config',
    slug: 'cloth-config',
    title: 'Cloth Config v15',
    author: 'shedaniel',
    source: 'CurseForge',
    downloads: '95,301,884',
    description: 'Configuration screen library for Minecraft mods.',
    icon: 'https://media.forgecdn.net/avatars/861/123/638268875569485121.png',
    dependencies: []
  },
  {
    id: 'appleskin',
    slug: 'appleskin',
    title: 'AppleSkin',
    author: 'squeek502',
    source: 'Modrinth',
    downloads: '42,109,230',
    description: 'Food/hunger HUD improvements for Minecraft. Shows saturation and heal points.',
    icon: 'https://cdn.modrinth.com/data/EsAfCjCV/icon.png',
    dependencies: []
  }
];

// Screen Navigation
function showScreen(screenId) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  const target = document.getElementById(screenId);
  if (target) target.classList.add('active');
  if (screenId === 'screen-controls') {
    renderVirtualButtons();
  }
}

// 1. Dashboard Version / Loader Selectors (Strict Lock)
function setVersion(ver) {
  state.version = ver;
  document.getElementById('btn-ver-1211').classList.toggle('active', ver === '1.21.1');
  document.getElementById('btn-ver-1201').classList.toggle('active', ver === '1.20.1');
  updateActivePill();
}

function setLoader(ldr) {
  state.loader = ldr;
  document.getElementById('btn-ldr-fabric').classList.toggle('active', ldr === 'fabric');
  document.getElementById('btn-ldr-neoforge').classList.toggle('active', ldr === 'neoforge');
  updateActivePill();
}

function updateActivePill() {
  const pill = document.getElementById('store-active-target');
  if (pill) {
    pill.innerText = `${state.version} • ${state.loader.toUpperCase()}`;
  }
}

// 2. XMods Store Search & Render
function renderModsList(query = '') {
  const container = document.getElementById('mods-container');
  container.innerHTML = '';

  const q = query.toLowerCase().trim();
  const filtered = sampleMods.filter(m => {
    // Filter by source
    if (state.activeSource === 'MODRINTH' && m.source !== 'Modrinth') return false;
    if (state.activeSource === 'CURSEFORGE' && m.source !== 'CurseForge') return false;
    // Filter by query
    if (q && !m.title.toLowerCase().includes(q) && !m.description.toLowerCase().includes(q)) return false;
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `<div style="text-align:center; padding: 40px; color:#71717a;">No mods found for "${query}".</div>`;
    return;
  }

  filtered.forEach(mod => {
    const isInstalled = state.installedMods.has(mod.id);
    const card = document.createElement('div');
    card.className = 'mod-card';
    card.innerHTML = `
      <img src="${mod.icon}" class="mod-avatar" alt="${mod.title}">
      <div class="mod-info">
        <div class="mod-title-row">
          <span class="mod-name">${mod.title}</span>
          <span class="badge-source ${mod.source.toLowerCase()}">${mod.source}</span>
        </div>
        <div class="mod-author">by ${mod.author} • ${mod.downloads} downloads</div>
        <div class="mod-desc">${mod.description}</div>
      </div>
      <button class="btn-mod-action ${isInstalled ? 'installed' : 'install'}" id="btn-mod-${mod.id}">
        ${isInstalled ? 'INSTALLED ✓' : 'INSTALL'}
      </button>
    `;

    // Clicking card opens Mod Detail & Dependency view
    card.addEventListener('click', (e) => {
      if (e.target.tagName.toLowerCase() === 'button') return;
      openModDetail(mod);
    });

    // Clicking Install / Installed button
    const btn = card.querySelector(`#btn-mod-${mod.id}`);
    btn.addEventListener('click', () => {
      if (!isInstalled) {
        installMod(mod);
      } else {
        openModDetail(mod);
      }
    });

    container.appendChild(card);
  });
}

function setFilterSource(source) {
  state.activeSource = source;
  document.getElementById('filter-all').classList.toggle('active', source === 'ALL');
  document.getElementById('filter-mr').classList.toggle('active', source === 'MODRINTH');
  document.getElementById('filter-cf').classList.toggle('active', source === 'CURSEFORGE');
  renderModsList(document.getElementById('input-mod-search').value);
}

// 3. Mod Installation & Duplicate Prevention
function installMod(mod) {
  if (state.installedMods.has(mod.id)) {
    alert(`[Duplicate Prevention]: ${mod.title} is already installed! Re-installation blocked.`);
    return;
  }

  // Check dependencies
  const missingDeps = mod.dependencies.filter(d => !state.installedMods.has(d.id));
  if (missingDeps.length > 0) {
    openModDetail(mod);
    return;
  }

  // Simulate download
  state.installedMods.add(mod.id);
  state.installedFiles.add(`${mod.slug}.jar`);
  renderModsList(document.getElementById('input-mod-search').value);
  alert(`✓ Installed ${mod.title} to .minecraft/mods/!`);
}

// 4. Mod Detail & Dependency Modal
function openModDetail(mod) {
  const modal = document.getElementById('detail-modal');
  modal.classList.add('active');

  document.getElementById('modal-mod-title').innerText = mod.title;
  document.getElementById('modal-mod-meta').innerText = `Source: ${mod.source} • by ${mod.author}`;
  document.getElementById('modal-mod-desc').innerText = mod.description;
  document.getElementById('modal-mod-icon').src = mod.icon;

  const isInstalled = state.installedMods.has(mod.id);
  const actionBtn = document.getElementById('modal-main-action');

  if (isInstalled) {
    actionBtn.innerText = 'UNINSTALL MOD (ALREADY INSTALLED)';
    actionBtn.style.background = '#991b1b';
    actionBtn.onclick = () => {
      state.installedMods.delete(mod.id);
      state.installedFiles.delete(`${mod.slug}.jar`);
      closeModal();
      renderModsList(document.getElementById('input-mod-search').value);
    };
  } else {
    actionBtn.innerText = 'INSTALL MOD';
    actionBtn.style.background = 'var(--crimson)';
    actionBtn.onclick = () => {
      state.installedMods.add(mod.id);
      state.installedFiles.add(`${mod.slug}.jar`);
      closeModal();
      renderModsList(document.getElementById('input-mod-search').value);
    };
  }

  // Render Dependencies
  const depList = document.getElementById('modal-dep-list');
  depList.innerHTML = '';

  if (!mod.dependencies || mod.dependencies.length === 0) {
    depList.innerHTML = '<div style="color:var(--green); font-size:12px; margin-top:10px;">✓ Zero dependencies required. Safe to install directly!</div>';
  } else {
    mod.dependencies.forEach(dep => {
      const depInstalled = state.installedMods.has(dep.id);
      const row = document.createElement('div');
      row.className = 'dep-item';
      row.innerHTML = `
        <div>
          <div class="dep-name">${dep.name}</div>
          <div class="dep-sub" style="color:${depInstalled ? 'var(--green)' : 'var(--crimson-glow)'};">
            ${depInstalled ? 'Status: Installed ✓' : 'Status: Missing (Required)'}
          </div>
        </div>
        <button class="btn-dep-dl ${depInstalled ? 'installed' : ''}" id="btn-dep-${dep.id}">
          ${depInstalled ? 'INSTALLED' : 'DOWNLOAD'}
        </button>
      `;

      const dlBtn = row.querySelector(`#btn-dep-${dep.id}`);
      if (!depInstalled) {
        dlBtn.onclick = () => {
          state.installedMods.add(dep.id);
          dlBtn.innerText = 'INSTALLED';
          dlBtn.classList.add('installed');
          row.querySelector('.dep-sub').innerText = 'Status: Installed ✓';
          row.querySelector('.dep-sub').style.color = 'var(--green)';
          renderModsList(document.getElementById('input-mod-search').value);
        };
      }

      depList.appendChild(row);
    });
  }
}

function closeModal() {
  document.getElementById('detail-modal').classList.remove('active');
}

// 5. Virtual Controls Canvas & Drag Editor
function renderVirtualButtons() {
  const surface = document.getElementById('controls-surface');
  surface.innerHTML = '';

  state.buttons.forEach(btn => {
    const el = document.createElement('div');
    el.className = `virtual-btn ${btn.round ? 'round' : ''} ${state.selectedBtn === btn ? 'selected' : ''}`;
    el.style.left = `${btn.x * 100}%`;
    el.style.top = `${btn.y * 100}%`;
    el.style.width = `${btn.w}px`;
    el.style.height = `${btn.h}px`;
    el.innerText = btn.label;

    // Dragging logic
    el.onmousedown = (e) => {
      e.stopPropagation();
      selectVirtualButton(btn);

      const rect = surface.getBoundingClientRect();
      const onMouseMove = (moveEvent) => {
        const mouseX = moveEvent.clientX - rect.left;
        const mouseY = moveEvent.clientY - rect.top;
        btn.x = Math.max(0.04, Math.min(0.96, mouseX / rect.width));
        btn.y = Math.max(0.06, Math.min(0.94, mouseY / rect.height));
        el.style.left = `${btn.x * 100}%`;
        el.style.top = `${btn.y * 100}%`;
      };

      const onMouseUp = () => {
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
      };

      window.addEventListener('mousemove', onMouseMove);
      window.addEventListener('mouseup', onMouseUp);
    };

    surface.appendChild(el);
  });
}

function selectVirtualButton(btn) {
  state.selectedBtn = btn;
  const bar = document.getElementById('bottom-control-props');
  if (btn) {
    bar.classList.add('active');
    document.getElementById('prop-btn-label').innerText = `Button: ${btn.label}`;
    document.getElementById('prop-btn-key').innerText = `Key: ${btn.key}`;
  } else {
    bar.classList.remove('active');
  }
  renderVirtualButtons();
}

function resizeSelectedButton(delta) {
  if (!state.selectedBtn) return;
  state.selectedBtn.w = Math.max(36, Math.min(130, state.selectedBtn.w + delta));
  state.selectedBtn.h = Math.max(36, Math.min(130, state.selectedBtn.h + delta));
  renderVirtualButtons();
}

function deleteSelectedButton() {
  if (!state.selectedBtn) return;
  state.buttons = state.buttons.filter(b => b !== state.selectedBtn);
  selectVirtualButton(null);
}

function showAddButtonModal() {
  const label = prompt("Enter button label (e.g., ZOOM, F3, MACRO, V):", "ZOOM");
  if (!label) return;
  const key = prompt("Enter GLFW key or Minecraft action (e.g., ZOOM (C), F3, ESCAPE):", "ZOOM (C)");
  
  const newBtn = {
    id: `btn_custom_${Date.now()}`,
    label: label.toUpperCase(),
    x: 0.5,
    y: 0.5,
    w: 64,
    h: 46,
    key: key || 'CUSTOM KEY',
    round: false
  };

  state.buttons.push(newBtn);
  selectVirtualButton(newBtn);
}

function resetDefaultControls() {
  state.buttons = [
    { id: 'btn_w', label: 'W', x: 0.12, y: 0.58, w: 60, h: 60, key: 'FORWARD (W)', round: false },
    { id: 'btn_a', label: 'A', x: 0.05, y: 0.72, w: 60, h: 60, key: 'LEFT (A)', round: false },
    { id: 'btn_s', label: 'S', x: 0.12, y: 0.72, w: 60, h: 60, key: 'BACK (S)', round: false },
    { id: 'btn_d', label: 'D', x: 0.19, y: 0.72, w: 60, h: 60, key: 'RIGHT (D)', round: false },
    { id: 'btn_sprint', label: 'SPRINT', x: 0.12, y: 0.44, w: 68, h: 40, key: 'SPRINT (Ctrl)', round: false },
    { id: 'btn_sneak', label: 'SNEAK', x: 0.12, y: 0.86, w: 68, h: 40, key: 'SNEAK (Shift)', round: false },
    { id: 'btn_jump', label: 'JUMP', x: 0.88, y: 0.74, w: 72, h: 72, key: 'JUMP (Space)', round: true },
    { id: 'btn_attack', label: 'ATTACK', x: 0.78, y: 0.60, w: 76, h: 76, key: 'ATTACK (L-Click)', round: true },
    { id: 'btn_use', label: 'USE', x: 0.88, y: 0.48, w: 72, h: 72, key: 'USE (R-Click)', round: true },
    { id: 'btn_inv', label: 'INV', x: 0.50, y: 0.88, w: 70, h: 44, key: 'INVENTORY (E)', round: false },
    { id: 'btn_f3', label: 'F3', x: 0.05, y: 0.10, w: 48, h: 36, key: 'DEBUG (F3)', round: false },
    { id: 'btn_f5', label: 'F5', x: 0.12, y: 0.10, w: 48, h: 36, key: 'CAMERA (F5)', round: false },
    { id: 'btn_zoom', label: 'ZOOM', x: 0.19, y: 0.10, w: 56, h: 36, key: 'ZOOM (C)', round: false },
    { id: 'btn_chat', label: 'CHAT', x: 0.50, y: 0.10, w: 60, h: 36, key: 'CHAT (T)', round: false },
    { id: 'btn_pause', label: 'ESC', x: 0.94, y: 0.10, w: 48, h: 36, key: 'PAUSE (Esc)', round: false }
  ];
  selectVirtualButton(null);
  alert("Controls reset to XM default layout!");
}

// Initialise
window.addEventListener('DOMContentLoaded', () => {
  renderModsList();
  updateActivePill();

  // Deselect button on clicking canvas surface
  document.getElementById('controls-surface').addEventListener('mousedown', (e) => {
    if (e.target.id === 'controls-surface') {
      selectVirtualButton(null);
    }
  });

  // Search input live handler
  document.getElementById('input-mod-search').addEventListener('input', (e) => {
    renderModsList(e.target.value);
  });
});

(() => {
  const tokenKey = 'salestrack.jwt';
  const userKey = 'salestrack.user';
  const page = document.body.dataset.page;

  const statusLabels = {
    NEW: 'Yeni',
    CONTACTED: 'İletişime geçildi',
    QUALIFIED: 'Nitelikli',
    DISQUALIFIED: 'Uygun değil',
    CONVERTED: 'Dönüştürüldü'
  };

  const stageLabels = {
    NEW: 'Yeni',
    QUALIFIED: 'Nitelikli',
    PROPOSAL: 'Teklif',
    NEGOTIATION: 'Müzakere',
    WON: 'Kazanıldı',
    LOST: 'Kaybedildi'
  };

  const getToken = () => sessionStorage.getItem(tokenKey);
  const getUser = () => JSON.parse(sessionStorage.getItem(userKey) || 'null');
  const loginPage = () => { window.location.href = '/login'; };

  const escapeHtml = value => String(value ?? '—').replace(/[&<>'"]/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;'
  }[c]));

  async function api(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: {
        Authorization: `Bearer ${getToken()}`,
        'Content-Type': 'application/json',
        ...(options.headers || {})
      }
    });
    if (response.status === 401) {
      sessionStorage.clear();
      loginPage();
      throw new Error('Oturumunuz sona erdi.');
    }
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      const validationMessage = body.validationErrors ? Object.values(body.validationErrors)[0] : null;
      throw new Error(validationMessage || body.message || body.error || 'İşlem tamamlanamadı.');
    }
    return response.status === 204 ? null : response.json();
  }

  const formatCurrency = value =>
    new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY', maximumFractionDigits: 0 }).format(value ?? 0);

  const greeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Günaydın';
    if (hour < 18) return 'İyi günler';
    return 'İyi akşamlar';
  };

  const firstNameFromEmail = email => {
    if (!email) return '—';
    const local = email.split('@')[0];
    const name = local.split(/[._-]/)[0];
    return name.charAt(0).toUpperCase() + name.slice(1);
  };

  function setupShell() {
    const user = getUser();
    if (!user) return;

    document.getElementById('user-email').textContent = user.email;
    document.getElementById('user-role').textContent = user.role || 'Kullanıcı';
    document.getElementById('user-initial').textContent = user.email.charAt(0).toUpperCase();
    const navKey = page === 'deal-detail' ? 'deals' : page;
    document.querySelector(`[data-nav="${navKey}"]`)?.classList.add('active');

    if (user.role === 'ADMIN') {
      const adminLink = document.getElementById('nav-admin-users');
      if (adminLink) adminLink.style.display = '';
    }

    document.getElementById('greeting-text') && (document.getElementById('greeting-text').textContent = greeting());
    document.getElementById('user-first-name') && (document.getElementById('user-first-name').textContent = firstNameFromEmail(user.email));

    document.getElementById('logout-button')?.addEventListener('click', () => {
      sessionStorage.clear();
      loginPage();
    });

    const sidebar = document.querySelector('.sidebar');
    const menuBtn = document.querySelector('.mobile-menu-btn');

    if (menuBtn && sidebar) {
      let overlay = document.querySelector('.sidebar-overlay');
      if (!overlay) {
        overlay = document.createElement('div');
        overlay.className = 'sidebar-overlay';
        document.body.appendChild(overlay);
      }

      const closeSidebar = () => {
        sidebar.classList.remove('open');
        overlay.classList.remove('open');
      };

      menuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('open');
      });

      overlay.addEventListener('click', closeSidebar);
    }
  }

  function renderPipelineStages(byStage, containerId = 'pipeline-stages') {
    const container = document.getElementById(containerId);
    if (!container) return;

    const openStages = ['NEW', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION'];
    const stages = openStages.map(stage => {
      const found = (byStage || []).find(s => s.stage === stage);
      return { stage, count: found?.count ?? 0, amount: found?.totalAmount ?? 0 };
    });

    const maxCount = Math.max(...stages.map(s => s.count), 1);

    if (stages.every(s => s.count === 0)) {
      container.innerHTML = '<div class="empty-state">Henüz açık fırsat bulunmuyor.</div>';
      return;
    }

    container.innerHTML = stages.map(s => `
      <div class="pipeline-row">
        <span class="pipeline-label">${stageLabels[s.stage] || s.stage}</span>
        <div class="pipeline-bar-wrap">
          <div class="pipeline-bar" style="width:${(s.count / maxCount) * 100}%"></div>
        </div>
        <div class="pipeline-meta">
          <strong>${s.count}</strong>
          ${s.amount > 0 ? formatCurrency(s.amount) : '—'}
        </div>
      </div>
    `).join('');
  }

  function renderLeadStatuses(leads) {
    const container = document.getElementById('lead-statuses');
    if (!container) return;

    const counts = leads.reduce((all, lead) => ({
      ...all,
      [lead.status]: (all[lead.status] || 0) + 1
    }), {});

    const max = Math.max(leads.length, 1);

    if (leads.length === 0) {
      container.innerHTML = '<div class="empty-state">Henüz lead eklenmemiş.</div>';
      return;
    }

    container.innerHTML = Object.entries(statusLabels).map(([status, label]) => `
      <div class="status-row">
        <span>${label}</span>
        <div class="progress">
          <div class="progress-bar" style="width:${((counts[status] || 0) / max) * 100}%"></div>
        </div>
        <strong>${counts[status] || 0}</strong>
      </div>
    `).join('');
  }

  function renderRecentLeads(leads) {
    const container = document.getElementById('recent-leads');
    if (!container) return;

    const recent = [...leads]
      .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
      .slice(0, 5);

    if (recent.length === 0) {
      container.innerHTML = '<div class="empty-state">Henüz lead eklenmemiş. <a href="/leads">İlk lead\'i ekleyin</a>.</div>';
      return;
    }

    container.innerHTML = recent.map(lead => `
      <a href="/leads" class="recent-lead-item">
        <span class="recent-lead-avatar">${escapeHtml(lead.name.charAt(0).toUpperCase())}</span>
        <span class="recent-lead-info">
          <strong>${escapeHtml(lead.name)}</strong>
          <small>${escapeHtml(lead.companyName || 'Firma belirtilmedi')}</small>
        </span>
        <span class="recent-lead-source">${escapeHtml(sourceLabel(lead.source))}</span>
        <span class="badge-status status-${String(lead.status).toLowerCase()}">${escapeHtml(statusLabels[lead.status] || lead.status)}</span>
      </a>
    `).join('');
  }

  async function loadDashboard() {
    const [leads, pipeline, won] = await Promise.all([
      api('/api/leads'),
      api('/api/reports/pipeline-summary'),
      api('/api/reports/won-summary')
    ]);

    document.getElementById('lead-count').textContent = leads.length;
    document.getElementById('open-deals').textContent = pipeline.totalOpenDeals ?? '—';

    const amountEl = document.getElementById('open-deals-amount');
    if (amountEl && pipeline.totalOpenAmount != null) {
      amountEl.textContent = `Pipeline değeri: ${formatCurrency(pipeline.totalOpenAmount)}`;
    }

    document.getElementById('won-total').textContent = formatCurrency(won.totalWonAmount ?? 0);

    const wonDetail = document.getElementById('won-detail');
    if (wonDetail) {
      wonDetail.textContent = `${won.wonDealCount ?? 0} kazanılan fırsat · ${won.month}/${won.year}`;
    }

    try {
      const conversion = await api('/api/reports/conversion-rate');
      document.getElementById('conversion-rate').textContent =
        `${Number(conversion.conversionRatePercentage ?? 0).toFixed(1)}%`;
    } catch (_) {
      document.getElementById('conversion-rate').textContent = '—';
      const detail = document.querySelector('.metric-orange .metric-detail');
      if (detail) detail.textContent = 'Yönetici yetkisi gerekli';
    }

    renderPipelineStages(pipeline.byStage);
    renderLeadStatuses(leads);
    renderRecentLeads(leads);
  }

  let leads = [];

  const sourceLabel = source => ({
    WEBSITE: 'Web sitesi',
    REFERRAL: 'Referans',
    COLD_CALL: 'Soğuk arama',
    OTHER: 'Diğer'
  }[source] || source || '—');

  function renderLeads() {
    const term = document.getElementById('lead-search').value.trim().toLocaleLowerCase('tr');
    const status = document.getElementById('lead-status-filter').value;
    const filtered = leads.filter(lead =>
      (!status || lead.status === status) &&
      (!term || [lead.name, lead.email, lead.companyName].some(value =>
        String(value || '').toLocaleLowerCase('tr').includes(term)
      ))
    );

    const tbody = document.getElementById('lead-table-body');
    tbody.innerHTML = filtered.length
      ? filtered.map(lead => `
        <tr>
          <td>
            <div class="lead-person">
              <strong>${escapeHtml(lead.name)}</strong>
              <small>${escapeHtml(lead.companyName || 'Firma belirtilmedi')}</small>
            </div>
          </td>
          <td>
            <div class="lead-person">
              <span>${escapeHtml(lead.email)}</span>
              <small>${escapeHtml(lead.phone)}</small>
            </div>
          </td>
          <td>${escapeHtml(sourceLabel(lead.source))}</td>
          <td>${renderLeadStatusCell(lead)}</td>
          <td>${escapeHtml(lead.assignedUserName || 'Atanmadı')}</td>
          <td><div class="row-actions">${renderLeadActions(lead)}</div></td>
        </tr>
      `).join('')
      : '<tr><td colspan="6" class="text-center text-secondary py-5">Eşleşen lead bulunamadı.</td></tr>';

    tbody.querySelectorAll('[data-lead-status]').forEach(select => {
      select.addEventListener('change', async () => {
        const leadId = Number(select.dataset.leadStatus);
        const previous = leads.find(l => l.id === leadId)?.status;
        try {
          await updateLeadStatus(leadId, select.value);
        } catch (error) {
          if (previous) select.value = previous;
          showToast(error.message, 'error');
        }
      });
    });

    tbody.querySelectorAll('[data-convert-lead]').forEach(btn =>
      btn.addEventListener('click', () => openConvertModal(Number(btn.dataset.convertLead)))
    );

    tbody.querySelectorAll('[data-delete-lead]').forEach(btn =>
      btn.addEventListener('click', () => deleteLead(Number(btn.dataset.deleteLead)))
    );
  }

  function renderLeadStatusCell(lead) {
    if (lead.status === 'CONVERTED') {
      return `<span class="badge-status status-converted">${statusLabels.CONVERTED}</span>`;
    }
    return `
      <select class="status-select status-${String(lead.status).toLowerCase()}" data-lead-status="${lead.id}">
        ${Object.entries(statusLabels)
          .filter(([value]) => value !== 'CONVERTED')
          .map(([value, label]) =>
            `<option value="${value}" ${lead.status === value ? 'selected' : ''}>${label}</option>`
          ).join('')}
      </select>
    `;
  }

  function renderLeadActions(lead) {
    if (lead.status === 'CONVERTED') {
      return `<a href="/deals" class="btn btn-sm btn-light"><i class="bi bi-kanban"></i> Fırsata git</a>`;
    }
    return `
      <button type="button" class="btn btn-sm btn-primary" data-convert-lead="${lead.id}"><i class="bi bi-arrow-right-circle"></i> Dönüştür</button>
      <button type="button" class="btn-icon danger" title="Sil" data-delete-lead="${lead.id}"><i class="bi bi-trash"></i></button>
    `;
  }

  async function deleteLead(id) {
    const lead = leads.find(l => l.id === id);
    if (!lead || !confirm(`"${lead.name}" adlı lead'i silmek istediğinize emin misiniz?`)) return;
    try {
      await api(`/api/leads/${id}`, { method: 'DELETE' });
      await loadLeads();
      showToast('Lead silindi.', 'success');
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  async function updateLeadStatus(leadId, status) {
    await api(`/api/leads/${leadId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    });
    await loadLeads();
    showToast('Lead durumu güncellendi.', 'success');
  }

  let convertCompanies = [];

  async function loadConvertCompanies() {
    convertCompanies = await api('/api/companies');
    populateCompanySelect(document.getElementById('convert-company'), convertCompanies);
  }

  function openConvertModal(leadId) {
    const lead = leads.find(l => l.id === leadId);
    if (!lead || lead.status === 'CONVERTED') return;

    document.getElementById('convert-lead-id').value = leadId;
    document.getElementById('convert-form-alert').classList.add('d-none');
    document.getElementById('convert-lead-title').textContent = `${lead.name} → Fırsat`;
    document.getElementById('convert-lead-info').textContent =
      `${lead.email || 'E-posta yok'} · ${lead.companyName || 'Firma belirtilmedi'}`;
    document.getElementById('convert-deal-title').value =
      lead.companyName ? `${lead.companyName} — ${lead.name}` : lead.name;
    document.getElementById('convert-deal-amount').value = '';
    document.getElementById('convert-deal-close').value = '';

    populateCompanySelect(document.getElementById('convert-company'), convertCompanies);
    const match = convertCompanies.find(c =>
      c.name.toLocaleLowerCase('tr') === String(lead.companyName || '').toLocaleLowerCase('tr')
    );
    if (match) document.getElementById('convert-company').value = match.id;

    bootstrap.Modal.getOrCreateInstance(document.getElementById('convertLeadModal')).show();
  }

  async function convertLead(event) {
    event.preventDefault();
    const alert = document.getElementById('convert-form-alert');
    alert.classList.add('d-none');

    const leadId = document.getElementById('convert-lead-id').value;
    const closeDate = document.getElementById('convert-deal-close').value;
    const payload = {
      companyId: Number(document.getElementById('convert-company').value),
      dealTitle: document.getElementById('convert-deal-title').value.trim(),
      amount: Number(document.getElementById('convert-deal-amount').value),
      expectedCloseDate: closeDate || null
    };

    try {
      await api(`/api/leads/${leadId}/convert`, { method: 'POST', body: JSON.stringify(payload) });
      bootstrap.Modal.getInstance(document.getElementById('convertLeadModal')).hide();
      await loadLeads();
      showToast('Lead başarıyla fırsata dönüştürüldü.', 'success');
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    }
  }

  async function loadLeads() {
    leads = await api('/api/leads');
    renderLeads();
  }

  async function createLead(event) {
    event.preventDefault();
    const alert = document.getElementById('lead-form-alert');
    alert.classList.add('d-none');

    const payload = {
      name: document.getElementById('lead-name').value.trim(),
      email: document.getElementById('lead-email').value.trim() || null,
      phone: document.getElementById('lead-phone').value.trim() || null,
      companyName: document.getElementById('lead-company').value.trim() || null,
      source: document.getElementById('lead-source').value
    };

    try {
      await api('/api/leads', { method: 'POST', body: JSON.stringify(payload) });
      event.target.reset();
      bootstrap.Modal.getInstance(document.getElementById('newLeadModal')).hide();
      await loadLeads();
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    }
  }

  const stageOrder = ['NEW', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'WON', 'LOST'];

  const allowedTransitions = {
    NEW: ['QUALIFIED', 'LOST'],
    QUALIFIED: ['NEW', 'PROPOSAL', 'LOST'],
    PROPOSAL: ['QUALIFIED', 'NEGOTIATION', 'LOST'],
    NEGOTIATION: ['PROPOSAL', 'WON', 'LOST'],
    WON: [],
    LOST: []
  };

  const stageDotClass = {
    NEW: 'stage-dot-new',
    QUALIFIED: 'stage-dot-qualified',
    PROPOSAL: 'stage-dot-proposal',
    NEGOTIATION: 'stage-dot-negotiation',
    WON: 'stage-dot-won',
    LOST: 'stage-dot-lost'
  };

  function showToast(message, type = 'error') {
    const toast = document.getElementById('app-toast') || document.getElementById('deal-toast');
    if (!toast) return;
    toast.textContent = message;
    toast.className = `app-toast ${type}`;
    clearTimeout(showToast._timer);
    showToast._timer = setTimeout(() => toast.classList.add('d-none'), 4000);
  }

  function populateCompanySelect(selectEl, companies, placeholder = 'Firma seçin…') {
    if (!selectEl) return;
    selectEl.innerHTML = `<option value="">${placeholder}</option>` +
      companies.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
  }

  /* ── Deals (Kanban) ───────────────────────────────────────── */
  let deals = [];

  function renderDealCard(deal) {
    const nextStages = allowedTransitions[deal.stage] || [];
    const stageControl = nextStages.length
      ? `<select class="deal-stage-select" data-deal-id="${deal.id}" data-current="${deal.stage}" aria-label="Aşama değiştir">
          <option value="${deal.stage}">${stageLabels[deal.stage]} (mevcut)</option>
          ${nextStages.map(s => `<option value="${s}">→ ${stageLabels[s]}</option>`).join('')}
        </select>`
      : `<span class="deal-stage-locked">${stageLabels[deal.stage]}</span>`;

    return `
      <div class="deal-card" draggable="true" data-deal-id="${deal.id}" data-stage="${deal.stage}">
        <div class="deal-card-header">
          <h4>${escapeHtml(deal.title)}</h4>
          <div class="deal-card-header-actions">
            <a href="/deals/${deal.id}" class="btn-icon" title="Detayları gör"><i class="bi bi-arrow-up-right"></i></a>
            <button type="button" class="btn-icon danger" title="Sil" data-delete-deal="${deal.id}"><i class="bi bi-trash"></i></button>
          </div>
        </div>
        <p class="deal-amount">${formatCurrency(deal.amount)}</p>
        <div class="deal-meta">
          <span><i class="bi bi-building"></i> ${escapeHtml(deal.companyName || '—')}</span>
          <span><i class="bi bi-person"></i> ${escapeHtml(deal.assignedUserName || 'Atanmadı')}</span>
        </div>
        <div class="deal-card-footer">
          <label class="deal-stage-label">Aşama</label>
          ${stageControl}
        </div>
      </div>
    `;
  }

  async function moveDealStage(dealId, fromStage, toStage) {
    const allowed = allowedTransitions[fromStage] || [];
    if (!allowed.includes(toStage)) {
      showToast(`"${stageLabels[fromStage]}" aşamasından "${stageLabels[toStage]}" aşamasına geçiş yapılamaz.`, 'error');
      return false;
    }
    try {
      await api(`/api/deals/${dealId}/stage`, {
        method: 'PATCH',
        body: JSON.stringify({ stage: toStage })
      });
      showToast(`Aşama güncellendi: ${stageLabels[toStage]}`, 'success');
      await loadDeals();
      return true;
    } catch (error) {
      showToast(error.message, 'error');
      return false;
    }
  }

  function renderKanban() {
    const board = document.getElementById('kanban-board');
    if (!board) return;

    if (deals.length === 0) {
      board.innerHTML = '<div class="empty-state py-5">Henüz fırsat yok. <button class="btn btn-primary btn-sm ms-2" data-bs-toggle="modal" data-bs-target="#newDealModal">İlk fırsatı ekle</button></div>';
      return;
    }

    board.innerHTML = stageOrder.map(stage => {
      const stageDeals = deals.filter(d => d.stage === stage);
      const total = stageDeals.reduce((sum, d) => sum + Number(d.amount || 0), 0);
      return `
        <div class="kanban-column">
          <div class="kanban-column-header">
            <span class="kanban-stage-dot ${stageDotClass[stage]}"></span>
            <h3>${stageLabels[stage]}</h3>
            <span class="kanban-count">${stageDeals.length}</span>
          </div>
          <div class="kanban-cards" data-stage="${stage}">
            ${stageDeals.length
              ? stageDeals.map(renderDealCard).join('')
              : `<div class="empty-state" style="padding:16px 0;font-size:12px">Boş · ${formatCurrency(total)}</div>`}
          </div>
        </div>
      `;
    }).join('');

    setupKanbanDragDrop();
    setupDealStageSelects();
    setupDealDeleteButtons();
  }

  function setupDealDeleteButtons() {
    document.querySelectorAll('[data-delete-deal]').forEach(btn => {
      btn.addEventListener('click', e => {
        e.stopPropagation();
        deleteDeal(Number(btn.dataset.deleteDeal));
      });
      btn.addEventListener('mousedown', e => e.stopPropagation());
    });
  }

  async function deleteDeal(id) {
    const deal = deals.find(d => d.id === id);
    if (!deal || !confirm(`"${deal.title}" fırsatını silmek istediğinize emin misiniz?`)) return;
    try {
      await api(`/api/deals/${id}`, { method: 'DELETE' });
      await loadDeals();
      showToast('Fırsat silindi.', 'success');
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  function setupDealStageSelects() {
    document.querySelectorAll('.deal-stage-select').forEach(select => {
      select.addEventListener('change', async () => {
        const dealId = Number(select.dataset.dealId);
        const fromStage = select.dataset.current;
        const toStage = select.value;
        if (toStage === fromStage) return;
        const ok = await moveDealStage(dealId, fromStage, toStage);
        if (!ok) select.value = fromStage;
      });
      select.addEventListener('mousedown', e => e.stopPropagation());
      select.addEventListener('click', e => e.stopPropagation());
    });
  }

  function setupKanbanDragDrop() {
    let draggedId = null;
    let draggedStage = null;

    document.querySelectorAll('.deal-card').forEach(card => {
      card.addEventListener('dragstart', e => {
        draggedId = card.dataset.dealId;
        draggedStage = card.dataset.stage;
        card.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', draggedId);
      });
      card.addEventListener('dragend', () => {
        card.classList.remove('dragging');
        document.querySelectorAll('.kanban-cards').forEach(col => col.classList.remove('drag-over'));
      });
    });

    document.querySelectorAll('.kanban-cards').forEach(column => {
      column.addEventListener('dragover', e => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        column.classList.add('drag-over');
      });
      column.addEventListener('dragleave', e => {
        if (!column.contains(e.relatedTarget)) column.classList.remove('drag-over');
      });
      column.addEventListener('drop', async e => {
        e.preventDefault();
        column.classList.remove('drag-over');
        const targetStage = column.dataset.stage;
        if (!draggedId || !targetStage || targetStage === draggedStage) return;
        await moveDealStage(Number(draggedId), draggedStage, targetStage);
      });
    });
  }

  async function loadDeals() {
    deals = await api('/api/deals');
    renderKanban();
  }

  async function loadDealCompanies() {
    const companies = await api('/api/companies');
    populateCompanySelect(document.getElementById('deal-company'), companies);
  }

  async function createDeal(event) {
    event.preventDefault();
    const alert = document.getElementById('deal-form-alert');
    alert.classList.add('d-none');

    const closeDate = document.getElementById('deal-close-date').value;
    const payload = {
      title: document.getElementById('deal-title').value.trim(),
      amount: Number(document.getElementById('deal-amount').value),
      companyId: Number(document.getElementById('deal-company').value),
      expectedCloseDate: closeDate || null
    };

    try {
      await api('/api/deals', { method: 'POST', body: JSON.stringify(payload) });
      event.target.reset();
      bootstrap.Modal.getInstance(document.getElementById('newDealModal')).hide();
      await loadDeals();
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    }
  }

  /* ── Deal Detail ──────────────────────────────────────────── */
  function getDealIdFromPath() {
    const parts = window.location.pathname.split('/').filter(Boolean);
    return Number(parts[parts.length - 1]);
  }

  function renderDealSummary(deal) {
    document.getElementById('deal-detail-title').textContent = deal.title;
    document.getElementById('deal-detail-stage').textContent = stageLabels[deal.stage] || deal.stage;
    document.getElementById('deal-detail-stage').className = `badge-status status-${String(deal.stage).toLowerCase()}`;
    document.getElementById('deal-detail-amount').textContent = formatCurrency(deal.amount);
    document.getElementById('deal-detail-company').textContent = deal.companyName || '—';
    document.getElementById('deal-detail-assigned').textContent = deal.assignedUserName || 'Atanmadı';
    document.getElementById('deal-detail-close-date').textContent =
      deal.expectedCloseDate ? new Intl.DateTimeFormat('tr-TR').format(new Date(deal.expectedCloseDate)) : '—';
    document.getElementById('deal-detail-created').textContent = formatDateTime(deal.createdAt);
  }

  function renderStageHistory(history) {
    const container = document.getElementById('deal-stage-history');
    if (!container) return;

    if (!history.length) {
      container.innerHTML = '<div class="empty-state py-4">Aşama geçmişi bulunamadı.</div>';
      return;
    }

    container.innerHTML = history.map(h => `
      <div class="stage-history-item">
        <span class="stage-history-dot"></span>
        <div class="stage-history-body">
          <strong>
            ${h.fromStage ? `${escapeHtml(stageLabels[h.fromStage] || h.fromStage)} → ` : ''}${escapeHtml(stageLabels[h.toStage] || h.toStage)}
          </strong>
          <div class="activity-meta">
            <span><i class="bi bi-clock"></i> ${formatDateTime(h.changedAt)}</span>
            <span><i class="bi bi-person-check"></i> ${escapeHtml(h.changedByName || '—')}</span>
          </div>
        </div>
      </div>
    `).join('');
  }

  function renderDealActivities(activityList) {
    const container = document.getElementById('deal-activity-list');
    if (!container) return;

    if (!activityList.length) {
      container.innerHTML = '<div class="empty-state py-4">Bu fırsata bağlı aktivite bulunmuyor.</div>';
      return;
    }

    container.innerHTML = activityList.map(activity => `
      <article class="activity-item">
        <span class="activity-icon ${activityTypeClasses[activity.type] || 'note'}">
          <i class="bi ${activityTypeIcons[activity.type] || 'bi-sticky-fill'}"></i>
        </span>
        <div class="activity-body">
          <strong>${escapeHtml(activityTypeLabels[activity.type] || activity.type)}</strong>
          <div class="activity-meta">
            <span><i class="bi bi-clock"></i> ${formatDateTime(activity.occurredAt)}</span>
            <span><i class="bi bi-person-check"></i> ${escapeHtml(activity.loggedByUserName || '—')}</span>
          </div>
          <p class="activity-description">${escapeHtml(activity.description)}</p>
        </div>
      </article>
    `).join('');
  }

  async function loadDealDetail() {
    const dealId = getDealIdFromPath();
    if (!dealId) {
      document.getElementById('deal-detail-title').textContent = 'Geçersiz fırsat';
      return;
    }

    try {
      const [deal, history, dealActivities] = await Promise.all([
        api(`/api/deals/${dealId}`),
        api(`/api/deals/${dealId}/stage-history`),
        api(`/api/deals/${dealId}/activities`)
      ]);
      renderDealSummary(deal);
      renderStageHistory(history);
      renderDealActivities(dealActivities);
    } catch (error) {
      showToast(error.message, 'error');
    }
  }

  /* ── Companies ────────────────────────────────────────────── */
  let companies = [];

  function renderCompanies() {
    const term = document.getElementById('company-search')?.value.trim().toLocaleLowerCase('tr') || '';
    const filtered = companies.filter(c =>
      !term || [c.name, c.sector, c.website].some(v => String(v || '').toLocaleLowerCase('tr').includes(term))
    );

    const tbody = document.getElementById('company-table-body');
    if (!tbody) return;

    tbody.innerHTML = filtered.length
      ? filtered.map(c => `
        <tr>
          <td>
            <div class="lead-person">
              <strong>${escapeHtml(c.name)}</strong>
            </div>
          </td>
          <td>${escapeHtml(c.sector || '—')}</td>
          <td>${c.website ? `<a href="${escapeHtml(c.website)}" class="company-link" target="_blank" rel="noopener">${escapeHtml(c.website)}</a>` : '—'}</td>
          <td>
            <div class="row-actions">
              <button class="btn-icon" title="Düzenle" data-edit-company="${c.id}"><i class="bi bi-pencil"></i></button>
              <button class="btn-icon danger" title="Sil" data-delete-company="${c.id}"><i class="bi bi-trash"></i></button>
            </div>
          </td>
        </tr>
      `).join('')
      : '<tr><td colspan="4" class="text-center text-secondary py-5">Eşleşen firma bulunamadı.</td></tr>';

    tbody.querySelectorAll('[data-edit-company]').forEach(btn =>
      btn.addEventListener('click', () => openCompanyModal(Number(btn.dataset.editCompany)))
    );
    tbody.querySelectorAll('[data-delete-company]').forEach(btn =>
      btn.addEventListener('click', () => deleteCompany(Number(btn.dataset.deleteCompany)))
    );
  }

  function openCompanyModal(id = null) {
    const modal = document.getElementById('companyModal');
    const form = document.getElementById('company-form');
    form.reset();
    document.getElementById('company-form-alert').classList.add('d-none');
    document.getElementById('company-id').value = id || '';

    if (id) {
      const company = companies.find(c => c.id === id);
      if (!company) return;
      document.getElementById('company-modal-eyebrow').textContent = 'DÜZENLE';
      document.getElementById('company-modal-title').textContent = 'Firmayı düzenle';
      document.getElementById('company-submit-btn').textContent = 'Güncelle';
      document.getElementById('company-name').value = company.name;
      document.getElementById('company-sector').value = company.sector || '';
      document.getElementById('company-website').value = company.website || '';
    } else {
      document.getElementById('company-modal-eyebrow').textContent = 'YENİ KAYIT';
      document.getElementById('company-modal-title').textContent = 'Firma ekle';
      document.getElementById('company-submit-btn').textContent = 'Kaydet';
    }

    bootstrap.Modal.getOrCreateInstance(modal).show();
  }

  async function loadCompanies() {
    companies = await api('/api/companies');
    renderCompanies();
  }

  async function saveCompany(event) {
    event.preventDefault();
    const alert = document.getElementById('company-form-alert');
    alert.classList.add('d-none');

    const id = document.getElementById('company-id').value;
    const payload = {
      name: document.getElementById('company-name').value.trim(),
      sector: document.getElementById('company-sector').value.trim() || null,
      website: document.getElementById('company-website').value.trim() || null
    };

    try {
      if (id) {
        await api(`/api/companies/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      } else {
        await api('/api/companies', { method: 'POST', body: JSON.stringify(payload) });
      }
      bootstrap.Modal.getInstance(document.getElementById('companyModal')).hide();
      await loadCompanies();
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    }
  }

  async function deleteCompany(id) {
    const company = companies.find(c => c.id === id);
    if (!company || !confirm(`"${company.name}" firmasını silmek istediğinize emin misiniz?`)) return;
    try {
      await api(`/api/companies/${id}`, { method: 'DELETE' });
      await loadCompanies();
    } catch (error) {
      alert(error.message);
    }
  }

  /* ── Admin: Users ─────────────────────────────────────────── */
  const roleLabels = { SALES_REP: 'Satış Temsilcisi', MANAGER: 'Yönetici', ADMIN: 'Admin' };
  let adminUsers = [];

  function renderAdminUsers() {
    const tbody = document.getElementById('admin-user-table-body');
    if (!tbody) return;

    tbody.innerHTML = adminUsers.length
      ? adminUsers.map(u => `
        <tr>
          <td><strong>${escapeHtml(u.name)}</strong></td>
          <td>${escapeHtml(u.email)}</td>
          <td>
            <select class="form-select form-select-sm" style="width:auto" data-role-select="${u.id}" ${u.email === getUser().email ? 'disabled title="Kendi rolünüzü değiştiremezsiniz"' : ''}>
              <option value="SALES_REP" ${u.role === 'SALES_REP' ? 'selected' : ''}>Satış Temsilcisi</option>
              <option value="MANAGER" ${u.role === 'MANAGER' ? 'selected' : ''}>Yönetici</option>
              <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
            </select>
          </td>
          <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString('tr-TR') : '—'}</td>
        </tr>
      `).join('')
      : '<tr><td colspan="4" class="text-center text-secondary py-5">Kullanıcı bulunamadı.</td></tr>';

    tbody.querySelectorAll('[data-role-select]').forEach(select =>
      select.addEventListener('change', () => updateUserRole(Number(select.dataset.roleSelect), select.value))
    );
  }

  async function loadAdminUsers() {
    adminUsers = await api('/api/users');
    renderAdminUsers();
  }

  async function updateUserRole(id, newRole) {
    const user = adminUsers.find(u => u.id === id);
    if (!user) return;
    if (!confirm(`"${user.name}" kullanıcısının rolünü "${roleLabels[newRole]}" olarak değiştirmek istediğinize emin misiniz?`)) {
      renderAdminUsers();
      return;
    }
    try {
      await api(`/api/users/${id}/role`, { method: 'PATCH', body: JSON.stringify({ role: newRole }) });
      await loadAdminUsers();
      showToast('Kullanıcı rolü güncellendi.', 'success');
    } catch (error) {
      showToast(error.message, 'error');
      await loadAdminUsers();
    }
  }

  /* ── Contacts ─────────────────────────────────────────────── */
  let contacts = [];
  let contactCompanies = [];

  function renderContacts() {
    const term = document.getElementById('contact-search')?.value.trim().toLocaleLowerCase('tr') || '';
    const companyId = document.getElementById('contact-company-filter')?.value || '';
    const filtered = contacts.filter(c =>
      (!companyId || String(c.companyId) === companyId) &&
      (!term || [c.name, c.email, c.position, c.companyName].some(v => String(v || '').toLocaleLowerCase('tr').includes(term)))
    );

    const tbody = document.getElementById('contact-table-body');
    if (!tbody) return;

    tbody.innerHTML = filtered.length
      ? filtered.map(c => `
        <tr>
          <td>
            <div class="lead-person">
              <strong>${escapeHtml(c.name)}</strong>
            </div>
          </td>
          <td>
            <div class="lead-person">
              <span>${escapeHtml(c.email)}</span>
              <small>${escapeHtml(c.phone || '—')}</small>
            </div>
          </td>
          <td>${escapeHtml(c.companyName || '—')}</td>
          <td>${escapeHtml(c.position || '—')}</td>
          <td>
            <div class="row-actions">
              <button class="btn-icon" title="Düzenle" data-edit-contact="${c.id}"><i class="bi bi-pencil"></i></button>
              <button class="btn-icon danger" title="Sil" data-delete-contact="${c.id}"><i class="bi bi-trash"></i></button>
            </div>
          </td>
        </tr>
      `).join('')
      : '<tr><td colspan="5" class="text-center text-secondary py-5">Eşleşen kişi bulunamadı.</td></tr>';

    tbody.querySelectorAll('[data-edit-contact]').forEach(btn =>
      btn.addEventListener('click', () => openContactModal(Number(btn.dataset.editContact)))
    );
    tbody.querySelectorAll('[data-delete-contact]').forEach(btn =>
      btn.addEventListener('click', () => deleteContact(Number(btn.dataset.deleteContact)))
    );
  }

  function openContactModal(id = null) {
    const modal = document.getElementById('contactModal');
    document.getElementById('contact-form').reset();
    document.getElementById('contact-form-alert').classList.add('d-none');
    document.getElementById('contact-id').value = id || '';
    populateCompanySelect(document.getElementById('contact-company'), contactCompanies);

    if (id) {
      const contact = contacts.find(c => c.id === id);
      if (!contact) return;
      document.getElementById('contact-modal-eyebrow').textContent = 'DÜZENLE';
      document.getElementById('contact-modal-title').textContent = 'Kişiyi düzenle';
      document.getElementById('contact-submit-btn').textContent = 'Güncelle';
      document.getElementById('contact-name').value = contact.name;
      document.getElementById('contact-email').value = contact.email;
      document.getElementById('contact-phone').value = contact.phone || '';
      document.getElementById('contact-position').value = contact.position || '';
      document.getElementById('contact-company').value = contact.companyId;
    } else {
      document.getElementById('contact-modal-eyebrow').textContent = 'YENİ KAYIT';
      document.getElementById('contact-modal-title').textContent = 'Kişi ekle';
      document.getElementById('contact-submit-btn').textContent = 'Kaydet';
    }

    bootstrap.Modal.getOrCreateInstance(modal).show();
  }

  async function loadContacts() {
    const [contactsData, companiesData] = await Promise.all([
      api('/api/contacts'),
      api('/api/companies')
    ]);
    contacts = contactsData;
    contactCompanies = companiesData;

    const filter = document.getElementById('contact-company-filter');
    if (filter) {
      filter.innerHTML = '<option value="">Tüm firmalar</option>' +
        companiesData.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
    }

    renderContacts();
  }

  async function saveContact(event) {
    event.preventDefault();
    const alert = document.getElementById('contact-form-alert');
    alert.classList.add('d-none');

    const id = document.getElementById('contact-id').value;
    const payload = {
      name: document.getElementById('contact-name').value.trim(),
      email: document.getElementById('contact-email').value.trim(),
      phone: document.getElementById('contact-phone').value.trim() || null,
      position: document.getElementById('contact-position').value.trim() || null,
      companyId: Number(document.getElementById('contact-company').value)
    };

    try {
      if (id) {
        await api(`/api/contacts/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      } else {
        await api('/api/contacts', { method: 'POST', body: JSON.stringify(payload) });
      }
      bootstrap.Modal.getInstance(document.getElementById('contactModal')).hide();
      await loadContacts();
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    }
  }

  async function deleteContact(id) {
    const contact = contacts.find(c => c.id === id);
    if (!contact || !confirm(`"${contact.name}" kişisini silmek istediğinize emin misiniz?`)) return;
    try {
      await api(`/api/contacts/${id}`, { method: 'DELETE' });
      await loadContacts();
    } catch (error) {
      alert(error.message);
    }
  }

  /* ── Activities ───────────────────────────────────────────── */
  let activities = [];
  let activityDeals = [];
  let activityContacts = [];

  const activityTypeLabels = {
    CALL: 'Arama',
    EMAIL: 'E-posta',
    MEETING: 'Toplantı',
    NOTE: 'Not'
  };

  const activityTypeIcons = {
    CALL: 'bi-telephone-fill',
    EMAIL: 'bi-envelope-fill',
    MEETING: 'bi-camera-video-fill',
    NOTE: 'bi-sticky-fill'
  };

  const activityTypeClasses = {
    CALL: 'call',
    EMAIL: 'email',
    MEETING: 'meeting',
    NOTE: 'note'
  };

  const formatDateTime = value => {
    if (!value) return '—';
    return new Intl.DateTimeFormat('tr-TR', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(value));
  };

  function renderActivities() {
    const container = document.getElementById('activity-timeline');
    if (!container) return;

    const term = document.getElementById('activity-search')?.value.trim().toLocaleLowerCase('tr') || '';
    const type = document.getElementById('activity-type-filter')?.value || '';
    const link = document.getElementById('activity-link-filter')?.value || '';

    const filtered = activities.filter(activity => {
      if (type && activity.type !== type) return false;
      if (link === 'deal' && !activity.dealId) return false;
      if (link === 'contact' && !activity.contactId) return false;
      if (!term) return true;
      const haystack = [
        activity.description,
        activity.dealTitle,
        activity.contactName,
        activity.loggedByUserName,
        activityTypeLabels[activity.type]
      ].map(v => String(v || '').toLocaleLowerCase('tr'));
      return haystack.some(v => v.includes(term));
    });

    if (!filtered.length) {
      container.innerHTML = '<div class="empty-state py-5">Eşleşen aktivite bulunamadı.</div>';
      return;
    }

    container.innerHTML = filtered.map(activity => `
      <article class="activity-item">
        <span class="activity-icon ${activityTypeClasses[activity.type] || 'note'}">
          <i class="bi ${activityTypeIcons[activity.type] || 'bi-sticky-fill'}"></i>
        </span>
        <div class="activity-body">
          <strong>${escapeHtml(activityTypeLabels[activity.type] || activity.type)}</strong>
          <div class="activity-meta">
            <span><i class="bi bi-clock"></i> ${formatDateTime(activity.occurredAt)}</span>
            ${activity.dealTitle ? `<span><i class="bi bi-kanban"></i> ${escapeHtml(activity.dealTitle)}</span>` : ''}
            ${activity.contactName ? `<span><i class="bi bi-person"></i> ${escapeHtml(activity.contactName)}</span>` : ''}
            <span><i class="bi bi-person-check"></i> ${escapeHtml(activity.loggedByUserName || '—')}</span>
          </div>
          <p class="activity-description">${escapeHtml(activity.description)}</p>
        </div>
      </article>
    `).join('');
  }

  function setupActivityLinkPicker() {
    const updateLinkFields = () => {
      const isDeal = document.querySelector('input[name="activity-link-type"]:checked')?.value === 'deal';
      document.getElementById('activity-deal-wrap')?.classList.toggle('d-none', !isDeal);
      document.getElementById('activity-contact-wrap')?.classList.toggle('d-none', isDeal);
    };

    document.querySelectorAll('input[name="activity-link-type"]').forEach(radio =>
      radio.addEventListener('change', updateLinkFields)
    );
    updateLinkFields();
  }

  async function loadActivityFormData() {
    const [deals, contacts] = await Promise.all([
      api('/api/deals'),
      api('/api/contacts')
    ]);
    activityDeals = deals;
    activityContacts = contacts;

    const dealSelect = document.getElementById('activity-deal');
    if (dealSelect) {
      dealSelect.innerHTML = '<option value="">Fırsat seçin…</option>' +
        deals.map(d => `<option value="${d.id}">${escapeHtml(d.title)} · ${formatCurrency(d.amount)}</option>`).join('');
    }

    const contactSelect = document.getElementById('activity-contact');
    if (contactSelect) {
      contactSelect.innerHTML = '<option value="">Kişi seçin…</option>' +
        contacts.map(c => `<option value="${c.id}">${escapeHtml(c.name)}${c.companyName ? ` (${escapeHtml(c.companyName)})` : ''}</option>`).join('');
    }
  }

  async function loadActivities() {
    activities = await api('/api/activities');
    renderActivities();
  }

  async function createActivity(event) {
    event.preventDefault();
    const alert = document.getElementById('activity-form-alert');
    alert.classList.add('d-none');

    const linkType = document.querySelector('input[name="activity-link-type"]:checked')?.value;
    const occurredRaw = document.getElementById('activity-occurred').value;
    const payload = {
      type: document.getElementById('activity-type').value,
      description: document.getElementById('activity-description').value.trim(),
      occurredAt: occurredRaw ? `${occurredRaw}:00` : null,
      dealId: linkType === 'deal' ? Number(document.getElementById('activity-deal').value) || null : null,
      contactId: linkType === 'contact' ? Number(document.getElementById('activity-contact').value) || null : null
    };

    if (linkType === 'deal' && !payload.dealId) {
      alert.textContent = 'Lütfen bir fırsat seçin.';
      alert.classList.remove('d-none');
      return;
    }
    if (linkType === 'contact' && !payload.contactId) {
      alert.textContent = 'Lütfen bir kişi seçin.';
      alert.classList.remove('d-none');
      return;
    }

    try {
      await api('/api/activities', { method: 'POST', body: JSON.stringify(payload) });
      event.target.reset();
      setupActivityLinkPicker();
      bootstrap.Modal.getInstance(document.getElementById('newActivityModal')).hide();
      await loadActivities();
      showToast('Aktivite kaydedildi.', 'success');
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    }
  }

  async function loadReports() {
    const [pipeline, won, leadsData] = await Promise.all([
      api('/api/reports/pipeline-summary'),
      api('/api/reports/won-summary'),
      api('/api/leads')
    ]);

    document.getElementById('report-open-deals').textContent = pipeline.totalOpenDeals ?? '—';
    document.getElementById('report-open-amount').textContent =
      `Pipeline değeri: ${formatCurrency(pipeline.totalOpenAmount)}`;
    document.getElementById('report-won-total').textContent = formatCurrency(won.totalWonAmount ?? 0);
    document.getElementById('report-won-detail').textContent =
      `${won.wonDealCount ?? 0} kazanılan fırsat · ${won.month}/${won.year}`;

    try {
      const conversion = await api('/api/reports/conversion-rate');
      document.getElementById('report-conversion').textContent =
        `${Number(conversion.conversionRatePercentage ?? 0).toFixed(1)}%`;
      document.getElementById('report-conversion-detail').textContent =
        `${conversion.convertedLeads ?? 0} / ${conversion.totalLeads ?? 0} lead dönüştürüldü`;
    } catch (_) {
      document.getElementById('report-conversion').textContent = '—';
      document.getElementById('report-conversion-detail').textContent = 'Yönetici yetkisi gerekli';
    }

    renderPipelineStages(pipeline.byStage, 'report-pipeline');

    const converted = leadsData.filter(l => l.status === 'CONVERTED').length;
    const qualified = leadsData.filter(l => l.status === 'QUALIFIED').length;
    document.getElementById('report-lead-summary').innerHTML = `
      <div class="report-stat-grid">
        <div class="report-stat">
          <span class="report-stat-value">${leadsData.length}</span>
          <span class="report-stat-label">Toplam lead</span>
        </div>
        <div class="report-stat">
          <span class="report-stat-value">${qualified}</span>
          <span class="report-stat-label">Nitelikli</span>
        </div>
        <div class="report-stat">
          <span class="report-stat-value">${converted}</span>
          <span class="report-stat-label">Dönüştürülen</span>
        </div>
      </div>
    `;

    const perfBody = document.getElementById('report-performance-body');
    const perfBadge = document.getElementById('report-perf-badge');
    try {
      const performance = await api('/api/reports/user-performance');
      perfBadge.textContent = 'Yönetici görünümü';
      perfBody.innerHTML = performance.length
        ? performance.map(row => `
          <tr>
            <td><strong>${escapeHtml(row.userName)}</strong></td>
            <td>${row.totalDeals ?? 0}</td>
            <td>${row.wonDeals ?? 0}</td>
            <td><strong>${formatCurrency(row.totalWonAmount)}</strong></td>
          </tr>
        `).join('')
        : '<tr><td colspan="4" class="text-center text-secondary py-5">Performans verisi bulunamadı.</td></tr>';
    } catch (_) {
      perfBadge.textContent = 'Yetki gerekli';
      perfBadge.className = 'badge-status status-disqualified';
      perfBody.innerHTML =
        '<tr><td colspan="4" class="text-center text-secondary py-5">Ekip performansı yalnızca yönetici ve admin kullanıcılar tarafından görüntülenebilir.</td></tr>';
    }
  }

  async function login(event) {
    event.preventDefault();
    const alert = document.getElementById('login-alert');
    const submit = document.getElementById('login-submit');
    alert.classList.add('d-none');
    submit.disabled = true;

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: document.getElementById('email').value.trim(),
          password: document.getElementById('password').value
        })
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'E-posta veya şifre hatalı.');
      sessionStorage.setItem(tokenKey, data.token);
      sessionStorage.setItem(userKey, JSON.stringify({ email: data.email, role: data.role }));
      window.location.href = '/dashboard';
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    } finally {
      submit.disabled = false;
    }
  }

  async function register(event) {
    event.preventDefault();
    const alert = document.getElementById('register-alert');
    const submit = document.getElementById('register-submit');
    alert.classList.add('d-none');
    submit.disabled = true;

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: document.getElementById('name').value.trim(),
          email: document.getElementById('email').value.trim(),
          password: document.getElementById('password').value
        })
      });
      const data = await response.json();
      if (!response.ok) {
        const message = data.validationErrors ? Object.values(data.validationErrors)[0] : (data.message || 'Kayıt oluşturulamadı.');
        throw new Error(message);
      }
      sessionStorage.setItem(tokenKey, data.token);
      sessionStorage.setItem(userKey, JSON.stringify({ email: data.email, role: data.role }));
      window.location.href = '/dashboard';
    } catch (error) {
      alert.textContent = error.message;
      alert.classList.remove('d-none');
    } finally {
      submit.disabled = false;
    }
  }

  document.addEventListener('DOMContentLoaded', async () => {
    if (document.getElementById('register-form')) {
      document.getElementById('register-form').addEventListener('submit', register);
      return;
    }

    if (document.getElementById('login-form')) {
      document.getElementById('login-form').addEventListener('submit', login);
      return;
    }

    if (!getToken()) {
      loginPage();
      return;
    }

    setupShell();

    try {
      if (page === 'dashboard') await loadDashboard();
      if (page === 'leads') {
        await Promise.all([loadLeads(), loadConvertCompanies()]);
        document.getElementById('lead-search').addEventListener('input', renderLeads);
        document.getElementById('lead-status-filter').addEventListener('change', renderLeads);
        document.getElementById('new-lead-form').addEventListener('submit', createLead);
        document.getElementById('convert-lead-form')?.addEventListener('submit', convertLead);
        document.getElementById('convertLeadModal')?.addEventListener('show.bs.modal', loadConvertCompanies);
      }
      if (page === 'deals') {
        await Promise.all([loadDeals(), loadDealCompanies()]);
        document.getElementById('new-deal-form').addEventListener('submit', createDeal);
        document.getElementById('newDealModal')?.addEventListener('show.bs.modal', loadDealCompanies);
      }
      if (page === 'companies') {
        await loadCompanies();
        document.getElementById('company-search').addEventListener('input', renderCompanies);
        document.getElementById('company-form').addEventListener('submit', saveCompany);
        document.getElementById('open-company-modal')?.addEventListener('click', () => openCompanyModal());
      }
      if (page === 'contacts') {
        await loadContacts();
        document.getElementById('contact-search').addEventListener('input', renderContacts);
        document.getElementById('contact-company-filter').addEventListener('change', renderContacts);
        document.getElementById('contact-form').addEventListener('submit', saveContact);
        document.getElementById('open-contact-modal')?.addEventListener('click', () => openContactModal());
      }
      if (page === 'reports') await loadReports();
      if (page === 'admin-users') await loadAdminUsers();
      if (page === 'deal-detail') await loadDealDetail();
      if (page === 'activities') {
        setupActivityLinkPicker();
        await Promise.all([loadActivities(), loadActivityFormData()]);
        document.getElementById('activity-search')?.addEventListener('input', renderActivities);
        document.getElementById('activity-type-filter')?.addEventListener('change', renderActivities);
        document.getElementById('activity-link-filter')?.addEventListener('change', renderActivities);
        document.getElementById('new-activity-form')?.addEventListener('submit', createActivity);
        document.getElementById('newActivityModal')?.addEventListener('show.bs.modal', loadActivityFormData);
      }
    } catch (error) {
      console.error(error);
    }
  });
})();

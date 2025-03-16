<template>
  <div class="landing-page">
    <!-- Hero section with background gradient -->
    <div class="hero-section">
      <div class="hero-content">
        <img src="@/assets/dei-logo.png" alt="DEI Técnico Lisboa" class="dei-logo">
        <h1 class="headline">{{ appName }}</h1>
        <p class="subheadline">Simplificado o percurso doutoral no DEI</p>
        <div class="hero-divider"></div>
        <p class="hero-description">
          Uma plataforma completa para a gestão de fluxos de trabalho de teses, agendamento de defesas, 
          e colaborações académicas ao longo do programa de doutoramento.
        </p>
      </div>
    </div>

    <!-- Features section -->
    <div class="features-section">
      <h2 class="section-title">Funcionalidades Principais</h2>
      
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">📝</div>
          <h3>Fluxo de Trabalho de Teses</h3>
          <p>Processo simplificado desde a proposta inicial até à submissão final da tese com notificações automáticas e monitorização de estado.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🎓</div>
          <h3>Gestão de Defesas</h3>
          <p>Agendamento simplificado, sistema de convite para júris e documentação centralizada para procedimentos de defesa.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">👨‍👩‍👧‍👦</div>
          <h3>Gestão de Pessoas</h3>
          <p>Diretório completo de alunos, professores e colaboradores externos com controlo de acesso baseado em funções.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">📊</div>
          <h3>Painel de Análise</h3>
          <p>Informações em tempo real sobre métricas do programa, progresso dos alunos e estatísticas departamentais.</p>
        </div>
      </div>
    </div>

    <!-- How it works section -->
    <div class="process-section">
      <h2 class="section-title">Como Funciona</h2>
      
      <div class="process-steps">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h3>Acesso com Credenciais Institucionais</h3>
            <p>Acesso seguro utilizando a sua conta institucional do DEI com permissões baseadas na função</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h3>Aceda ao Seu Painel Personalizado</h3>
            <p>Visualize informações e ações adaptadas à sua função no programa</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h3>Gerencie os Seus Fluxos de Trabalho</h3>
            <p>Acompanhe o progresso, submeta documentos e colabore com orientadores e membros do comité</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer section -->
    <footer class="footer">
      <p>&copy; {{ currentYear }} Departamento de Engenharia Informática (DEI) - Técnico Lisboa</p>
      <p class="footer-links">
        <a href="#">Política de Privacidade</a> • 
        <a href="#">Termos de Serviço</a> • 
        <a href="#">Suporte</a>
      </p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// Set application name from environment variable
const appName = import.meta.env.VITE_NAME || "PhD Management System";

// Get current year for footer
const currentYear = computed(() => new Date().getFullYear());
</script>

<style scoped>
/* Modern color palette and variables */
:root {
  --primary: #2563eb;
  --primary-dark: #1d4ed8;
  --primary-light: #60a5fa;
  --accent: #f97316;
  --bg-light: #f8fafc;
  --bg-white: #ffffff;
  --text-dark: #1e293b;
  --text-medium: #475569;
  --text-light: #94a3b8;
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --radius: 12px;
}

/* Base styles */
.landing-page {
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  background-color: var(--bg-light);
  color: var(--text-dark);
  line-height: 1.6;
}

/* Hero section */
.hero-section {
  background: linear-gradient(120deg, #3b82f6, #1e40af);
  color: white;
  padding: 6rem 2rem;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.dei-logo {
  height: 64px;
  margin-bottom: 2rem;
}

.headline {
  font-size: 3rem;
  font-weight: 800;
  letter-spacing: -0.025em;
  line-height: 1.2;
  margin-bottom: 1rem;
}

.subheadline {
  font-size: 1.5rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  opacity: 0.9;
}

.hero-divider {
  width: 80px;
  height: 4px;
  background-color: var(--accent);
  margin: 1.5rem auto;
  border-radius: 2px;
}

.hero-description {
  font-size: 1.125rem;
  max-width: 600px;
  margin: 0 auto;
  opacity: 0.9;
}

/* Features section */
.features-section {
  padding: 5rem 2rem;
  background-color: var(--bg-white);
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 3rem;
  color: var(--text-dark);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  background-color: var(--bg-light);
  border-radius: var(--radius);
  padding: 2rem;
  box-shadow: var(--shadow-md);
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--primary);
}

.feature-card p {
  color: var(--text-medium);
  font-size: 0.95rem;
}

/* Process section */
.process-section {
  padding: 5rem 2rem;
  background-color: #e2e8f0;
}

.process-steps {
  max-width: 850px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
  background-color: white;
  border-radius: var(--radius);
  padding: 3rem;
  box-shadow: var(--shadow-md);
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
}

.step-number {
  background-color: var(--primary-dark);
  color: black;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.5rem;
  flex-shrink: 0;
  box-shadow: 0 3px 5px rgba(0, 0, 0, 0.2);
  border: 2px solid white;
}

.step-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-dark);
}

.step-content p {
  color: var(--text-medium);
}

/* Footer */
.footer {
  background-color: var(--text-dark);
  color: white;
  padding: 3rem 2rem;
  text-align: center;
}

.footer p {
  opacity: 0.8;
  margin-bottom: 1rem;
}

.footer-links {
  font-size: 0.875rem;
}

.footer-links a {
  color: var(--primary-light);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: white;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .headline {
    font-size: 2.25rem;
  }
  
  .subheadline {
    font-size: 1.25rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .process-steps {
    padding: 1.5rem;
    gap: 3.5rem;
  }
  
  .step {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .step-number {
    margin-bottom: 1rem;
  }
  
  .step:not(:last-child)::after {
    left: 50%;
    top: 48px;
    transform: translateX(-50%);
    height: calc(100% - 48px);
  }
}
</style>
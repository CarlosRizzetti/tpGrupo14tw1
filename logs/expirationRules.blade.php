
@extends('layouts.app')

@section('content')
    <div class="{{ $category->styles['theme'] }} flex flex-col h-screen bg-puesto-app transition-colors duration-300 font-sans text-gray-800">

        {{--  CABECERA --}}
        @section('header_left')
            <a href="{{ route('category.products', $category) }}" class="flex items-center px-4 py-2 bg-black/10 hover:bg-black/20 rounded-lg font-bold uppercase text-xs border border-white/20 transition">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M15 19l-7-7 7-7" />
                </svg>
                Volver
            </a>
        @endsection

        @section('header_center')
            <div class="flex flex-col">
                <span class="block text-[10px] font-bold opacity-70 uppercase tracking-widest leading-none mb-1">Configurar Vencimiento</span>
                <h2 class="text-2xl md:text-3xl font-black uppercase italic tracking-tighter leading-none">
                    {{ $product->name }}
                </h2>
            </div>
        @endsection


        {{--  GRILLA DE REGLAS --}}
        <div class="flex-grow p-4 overflow-y-auto">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 pb-20">
                @foreach($rules as $rule)
                    <div class="bg-white rounded-[2rem] shadow-xl overflow-hidden flex flex-col border border-gray-100">
                        <form action="{{ route('print.ticket', $rule) }}"
                              method="POST"
                              class="flex flex-col h-full"
                              id="form-{{ $rule->id }}"
                              onsubmit="return processAndSubmit({{ $rule->id }})">
                            @csrf
                            <input type="hidden" name="rule_id" value="{{ $rule->id }}">
                            <input type="hidden" name="offset_minutes" id="final-{{ $rule->id }}" value="0">
                            <input type="hidden" name="category_id" value="{{$category->id}}">

                            {{-- UBICACIÓN --}}
                            <div class="bg-gray-800 text-white p-4 text-center">
                                <h3 class="text-lg font-black uppercase tracking-tight truncate">{{ $rule->location }}</h3>
                            </div>

                            {{-- CÁLCULOS --}}
                            <div class="p-6 flex-grow space-y-6">
                                <div class="text-center">
                                    <span class="text-gray-400 text-xs font-bold uppercase tracking-widest">Vencimiento Estándar:</span>
                                    <div class="text-4xl font-black text-puesto-header tracking-tighter">
                                        {{ now()->addMinutes($rule->duration_minutes)->format('d/m H:i') }}
                                    </div>
                                </div>

                                {{-- SELECTOR INTELIGENTE --}}
                                <div class="bg-gray-50 p-5 rounded-3xl border border-gray-100 space-y-4">
                                    <label class="block text-[10px] font-bold text-gray-400 uppercase text-center tracking-widest italic">
                                        ¿Hace cuánto se elaboró?
                                    </label>

                                    <div class="flex flex-col gap-3">
                                        <select id="unit-{{ $rule->id }}" onchange="toggleInputs({{ $rule->id }})"
                                                class="w-full text-center text-sm font-black bg-white border-2 border-gray-200 rounded-2xl p-3 uppercase focus:outline-none focus:border-puesto-header transition-all">
                                            <option value="1">Restar Minutos</option>
                                            <option value="60">Restar Horas</option>
                                            <option value="1440">Restar Días</option>
                                            <option value="custom">📅 Elegir Hora Exacta</option>
                                        </select>

                                        <div id="numeric-container-{{ $rule->id }}" class="relative">
                                            <input type="number" id="val-{{ $rule->id }}" value="0" min="0"
                                                   class="w-full text-center text-4xl font-black bg-white border-2 border-gray-200 rounded-2xl p-3 focus:outline-none focus:border-puesto-header text-gray-700">
                                            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-300 font-bold text-xs uppercase italic">Tiempo</span>
                                        </div>

                                        <div id="date-container-{{ $rule->id }}" class="hidden">
                                            <input type="datetime-local" id="date-{{ $rule->id }}"
                                                   class="w-full text-center text-sm font-bold text-gray-600 bg-white border-2 border-gray-200 rounded-2xl p-4 focus:outline-none focus:border-puesto-header">
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {{-- BOTÓN IMPRIMIR --}}
                            <div class="p-4 bg-gray-50/50">
                                <button type="submit"
                                        class="w-full py-5 bg-puesto-btn text-puesto-btn-text border-b-8 border-puesto-btn-border
                               rounded-[1.5rem] text-2xl font-black uppercase tracking-tight
                               active:border-b-0 active:translate-y-2 transition-all flex items-center justify-center gap-3">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 00-2 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
                                    </svg>
                                    IMPRIMIR
                                </button>
                            </div>
                        </form>
                    </div>
                @endforeach
            </div>
        </div>
    </div>
@endsection
@push('scripts')
    @vite('resources/js/calculateOffsetMinutes.js')
@endpush

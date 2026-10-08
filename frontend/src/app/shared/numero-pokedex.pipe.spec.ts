import { NumeroPokedexPipe } from './numero-pokedex.pipe';

describe('NumeroPokedexPipe', () => {
  const pipe = new NumeroPokedexPipe();

  it('completa con ceros hasta cuatro cifras', () => {
    expect(pipe.transform(1)).toBe('N.º 0001');
    expect(pipe.transform(25)).toBe('N.º 0025');
    expect(pipe.transform(151)).toBe('N.º 0151');
  });

  it('devuelve texto vacío si no hay número', () => {
    expect(pipe.transform(null)).toBe('');
  });
});
